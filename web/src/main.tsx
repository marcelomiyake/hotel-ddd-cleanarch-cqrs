import React from "react";
import { createRoot } from "react-dom/client";
import { HotelHttpGateway, ReservationHttpGateway } from "./infrastructure/api-gateways";
import { App } from "./presentation/App";
import "./presentation/styles.css";

const root = document.getElementById("root");
if (!root) throw new Error("The page root element is missing.");

const hotels = new HotelHttpGateway();
const reservations = new ReservationHttpGateway();
createRoot(root).render(
  <React.StrictMode>
    <App hotelGateway={hotels} reservationGateway={reservations} reservationFunnelGateway={reservations} />
  </React.StrictMode>,
);
