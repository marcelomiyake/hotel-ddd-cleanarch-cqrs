import { lazy, Suspense, useEffect, useMemo, useRef, useState } from "react";
import type { HotelReadPort, ReservationCommandPort, ReservationFunnelEventPort, ReservationReadPort } from "../application/ports";
import { GetHotelDetailsQuery, FindAvailableRoomsQuery, SearchHotelsQuery } from "../application/hotel-queries";
import { CancelReservationCommand, FindReservationsQuery, PlaceReservationCommand } from "../application/reservation-operations";
import { getOrCreateReservationFunnelSessionId, ReservationFunnelEventRecorder } from "../application/reservation-funnel-tracking";
import type { HotelCard, HotelDetails, RoomAvailability, RoomType } from "../domain/hotel";
import type { Reservation } from "../domain/reservation";
import { defaultSearch, validateSearch } from "../domain/stay";
import { HomePage } from "./HomePage";
import { ErrorNotice, Footer, SiteHeader } from "./shell-components";

const SearchResultsPage = lazy(() => import("./pages").then((module) => ({ default: module.SearchResultsPage })));
const HotelDetailsPage = lazy(() => import("./pages").then((module) => ({ default: module.HotelDetailsPage })));
const CheckoutPage = lazy(() => import("./pages").then((module) => ({ default: module.CheckoutPage })));
const ConfirmationPage = lazy(() => import("./pages").then((module) => ({ default: module.ConfirmationPage })));
const TripsPage = lazy(() => import("./pages").then((module) => ({ default: module.TripsPage })));

type Screen = "home" | "results" | "hotel" | "checkout" | "confirmation" | "trips";
type TrackedAttempt = { readonly attemptId: string | null; readonly screen: Screen | null; readonly completed: boolean };

type AppProps = {
  readonly hotelGateway: HotelReadPort;
  readonly reservationGateway: ReservationReadPort & ReservationCommandPort;
  readonly reservationFunnelGateway: ReservationFunnelEventPort;
};

export function App({ hotelGateway, reservationGateway, reservationFunnelGateway }: AppProps) {
  const [screen, setScreen] = useState<Screen>("home");
  const [funnelSessionId] = useState(() => getOrCreateReservationFunnelSessionId(
    window.sessionStorage, () => window.crypto.randomUUID(),
  ));
  const [reservationAttemptId, setReservationAttemptId] = useState<string | null>(null);
  const trackedAttempt = useRef<TrackedAttempt>({ attemptId: null, screen: null, completed: false });
  const [criteria, setCriteria] = useState(defaultSearch);
  const [hotels, setHotels] = useState<HotelCard[]>([]);
  const [availability, setAvailability] = useState<RoomAvailability[]>([]);
  const [hotelDetails, setHotelDetails] = useState<HotelDetails | null>(null);
  const [selectedRoom, setSelectedRoom] = useState<RoomType | null>(null);
  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [guestName, setGuestName] = useState("");
  const [guestEmail, setGuestEmail] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const searchHotels = useMemo(() => new SearchHotelsQuery(hotelGateway), [hotelGateway]);
  const getHotelDetails = useMemo(() => new GetHotelDetailsQuery(hotelGateway), [hotelGateway]);
  const findAvailableRooms = useMemo(() => new FindAvailableRoomsQuery(reservationGateway), [reservationGateway]);
  const placeReservation = useMemo(() => new PlaceReservationCommand(reservationGateway), [reservationGateway]);
  const findReservations = useMemo(() => new FindReservationsQuery(reservationGateway), [reservationGateway]);
  const cancelReservation = useMemo(() => new CancelReservationCommand(reservationGateway), [reservationGateway]);
  const funnelEventRecorder = useMemo(
    () => new ReservationFunnelEventRecorder(reservationFunnelGateway), [reservationFunnelGateway],
  );

  useEffect(() => {
    const titleByScreen: Record<Screen, string> = {
      home: "Wayfarer — Considered stays across Brazil",
      results: `Stays in ${criteria.city} — Wayfarer`,
      hotel: hotelDetails ? `${hotelDetails.hotel.name} — Wayfarer` : "Hotel details — Wayfarer",
      checkout: "Your reservation — Wayfarer",
      confirmation: "Reservation confirmed — Wayfarer",
      trips: "My trips — Wayfarer",
    };
    document.title = titleByScreen[screen];
  }, [criteria.city, hotelDetails, screen]);

  useEffect(() => {
    if (!reservationAttemptId) return;
    const cursor = trackedAttempt.current;
    const eventDetails = {
      sessionId: funnelSessionId,
      reservationAttemptId,
      screen,
      hotelId: hotelDetails?.hotel.id,
      roomTypeId: selectedRoom?.id,
    };

    if (cursor.attemptId !== reservationAttemptId) {
      trackedAttempt.current = { attemptId: reservationAttemptId, screen, completed: false };
      void funnelEventRecorder.record({ ...eventDetails, eventType: "RESERVATION_STARTED" });
      return;
    }
    if (cursor.completed || cursor.screen === screen) return;

    const confirmed = screen === "confirmation" && reservation !== null;
    trackedAttempt.current = { attemptId: reservationAttemptId, screen, completed: confirmed };
    void funnelEventRecorder.record({
      ...eventDetails,
      eventType: confirmed ? "RESERVATION_CONFIRMED" : "SCREEN_VIEWED",
    });
  }, [funnelEventRecorder, funnelSessionId, hotelDetails?.hotel.id, reservation, reservationAttemptId, screen, selectedRoom?.id]);

  useEffect(() => {
    let active = true;
    searchHotels.execute("Rio de Janeiro")
      .then((items) => { if (active) setHotels(items); })
      .catch(() => { if (active) setHotels([]); });
    return () => { active = false; };
  }, [searchHotels]);

  async function runSearch(nextCriteria = criteria) {
    const validationError = validateSearch(nextCriteria);
    if (validationError) {
      setError(validationError);
      return;
    }
    setBusy(true);
    setError("");
    setCriteria(nextCriteria);
    try {
      const [hotelResults, roomResults] = await Promise.all([
        searchHotels.execute(nextCriteria.city),
        findAvailableRooms.execute(nextCriteria),
      ]);
      setHotels(hotelResults);
      setAvailability(roomResults);
      setScreen("results");
    } catch (cause) {
      setError(errorMessage(cause, "We couldn’t search just now. Please try again."));
    } finally {
      setBusy(false);
    }
  }

  async function openHotel(hotel: HotelCard) {
    setBusy(true);
    setError("");
    try {
      const details = await getHotelDetails.execute(hotel.id);
      setHotelDetails(details);
      const roomResults = await findAvailableRooms.execute(criteria);
      setAvailability(roomResults);
      setSelectedRoom(null);
      setScreen("hotel");
    } catch (cause) {
      setError(errorMessage(cause, "We couldn’t load this stay. Please try again."));
    } finally {
      setBusy(false);
    }
  }

  function chooseRoom(room: RoomType) {
    if (!reservationAttemptId || trackedAttempt.current.completed) {
      setReservationAttemptId(window.crypto.randomUUID());
    }
    setSelectedRoom(room);
    setError("");
    setScreen("checkout");
  }

  async function confirmReservation() {
    if (!hotelDetails || !selectedRoom) return;
    setBusy(true);
    setError("");
    try {
      const requestKey = window.crypto.randomUUID();
      const saved = await placeReservation.execute({
        ...criteria,
        hotelId: hotelDetails.hotel.id,
        roomTypeId: selectedRoom.id,
        guestName,
        guestEmail,
      }, requestKey);
      setReservation(saved);
      setGuestEmail(saved.guestEmail);
      setScreen("confirmation");
    } catch (cause) {
      setError(errorMessage(cause, "We couldn’t complete your reservation. Please try again."));
    } finally {
      setBusy(false);
    }
  }

  async function lookupReservations(email: string) {
    setBusy(true);
    setError("");
    setGuestEmail(email.trim());
    try {
      setReservations(await findReservations.execute(email));
    } catch (cause) {
      setReservations([]);
      setError(errorMessage(cause, "We couldn’t find those trips. Check the email and try again."));
    } finally {
      setBusy(false);
    }
  }

  async function cancelSavedReservation(item: Reservation) {
    setBusy(true);
    setError("");
    try {
      const cancelled = await cancelReservation.execute(item.id, item.guestEmail);
      setReservations((previous) => previous.map((saved) => saved.id === cancelled.id ? cancelled : saved));
    } catch (cause) {
      setError(errorMessage(cause, "We couldn’t cancel this reservation. Please try again."));
    } finally {
      setBusy(false);
    }
  }

  function showHome() {
    setError("");
    setScreen("home");
  }

  function showTrips() {
    setError("");
    setReservations([]);
    setScreen("trips");
  }

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">Skip to content</a>
      <SiteHeader onHome={showHome} onTrips={showTrips} current={screen === "trips" ? "trips" : "stays"} />
      <main id="main-content" className="main-content">
        {screen !== "checkout" && screen !== "trips" && <ErrorNotice message={error} />}
        {screen === "home" && <HomePage criteria={criteria} onCriteriaChange={setCriteria} onSearch={() => void runSearch()}
          onSelectHotel={(hotel) => void openHotel(hotel)} hotels={hotels} availability={availability} busy={busy} />}
        <Suspense fallback={<output className="page-width page-loading" aria-live="polite">Loading your stay…</output>}>
          {screen === "results" && <SearchResultsPage criteria={criteria} onCriteriaChange={setCriteria} onSearch={() => void runSearch()}
            onSelectHotel={(hotel) => void openHotel(hotel)} hotels={hotels} availability={availability} busy={busy} />}
          {screen === "hotel" && hotelDetails && <HotelDetailsPage details={hotelDetails} criteria={criteria}
            availability={availability} selectedRoomId={selectedRoom?.id ?? ""} onSelectRoom={chooseRoom}
            onBack={() => setScreen("results")} />}
          {screen === "checkout" && hotelDetails && selectedRoom && <CheckoutPage details={hotelDetails} room={selectedRoom}
            criteria={criteria} guestName={guestName} guestEmail={guestEmail} error={error} busy={busy}
            onGuestName={setGuestName} onGuestEmail={setGuestEmail} onSubmit={() => void confirmReservation()}
            onBack={() => { setError(""); setScreen("hotel"); }} />}
          {screen === "confirmation" && reservation && <ConfirmationPage reservation={reservation}
            hotel={hotels.find((item) => item.id === reservation.hotelId)} onHome={showHome} onTrips={showTrips} />}
          {screen === "trips" && <TripsPage hotels={hotels} reservations={reservations} initialEmail={guestEmail}
            busy={busy} error={error} onFind={(email) => void lookupReservations(email)}
            onCancel={(item) => void cancelSavedReservation(item)} />}
        </Suspense>
      </main>
      <Footer />
    </div>
  );
}

function errorMessage(cause: unknown, fallback: string): string {
  return cause instanceof Error ? cause.message : fallback;
}
