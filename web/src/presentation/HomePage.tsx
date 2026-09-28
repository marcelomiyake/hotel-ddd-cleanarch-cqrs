import type { HotelCard, RoomAvailability } from "../domain/hotel";
import type { SearchCriteria } from "../domain/stay";
import { HotelCardView, SearchForm } from "./home-components";

type HomePageProps = {
  readonly criteria: SearchCriteria;
  readonly onCriteriaChange: (next: SearchCriteria) => void;
  readonly onSearch: () => void;
  readonly onSelectHotel: (hotel: HotelCard) => void;
  readonly hotels: HotelCard[];
  readonly availability: RoomAvailability[];
  readonly busy: boolean;
};

export function HomePage(props: HomePageProps) {
  const picks = props.hotels.slice(0, 3);
  return (
    <>
      <section className="hero page-width" aria-labelledby="hero-title">
        <div className="hero-copy">
          <p className="eyebrow"><span className="eyebrow-line" /> Stay a little closer</p>
          <h1 id="hero-title">Places with<br />a point of view<span className="brand-period">.</span></h1>
          <p className="hero-description">Independent hotels, chosen for the feeling they leave you with.</p>
          <div className="hero-proof">
            <span className="proof-star" aria-hidden="true">✳</span>
            <span>Thoughtful stays. Straightforward booking.</span>
          </div>
        </div>
        <div className="hero-image-wrap">
          <img className="hero-image" src="/hotels/hero.webp" alt="A quiet hotel pool overlooking a tropical coast" width="740" height="493" fetchPriority="high" />
          <div className="hero-caption"><span>01 / 06</span><span>Rio, at your own pace</span><span aria-hidden="true">↗</span></div>
          <div className="hero-image-stamp" aria-hidden="true">A stay<br />worth<br />slowing<br />down for.</div>
        </div>
      </section>
      <section className="search-shell page-width" aria-label="Find your stay">
        <SearchForm criteria={props.criteria} onChange={props.onCriteriaChange} onSearch={props.onSearch} busy={props.busy} />
      </section>
      <section className="intro-strip page-width" aria-label="Why book with Wayfarer">
        <p><span className="intro-index">01</span> Independent by nature</p>
        <p><span className="intro-index">02</span> Picked by people, not algorithms</p>
        <p><span className="intro-index">03</span> The price you see is the price you pay</p>
      </section>
      <section className="section-block page-width" aria-labelledby="collection-title">
        <div className="section-heading">
          <div>
            <p className="eyebrow">A good place to begin</p>
            <h2 id="collection-title">The Brazil collection</h2>
          </div>
          <p className="section-aside">Small places. Big sense of place.</p>
        </div>
        {picks.length > 0 ? (
          <div className="hotel-grid">
            {picks.map((hotel) => <HotelCardView key={hotel.id} hotel={hotel} availability={props.availability} onSelect={props.onSelectHotel} />)}
          </div>
        ) : (
          <div className="collection-note">
            <span className="collection-note-mark" aria-hidden="true">✳</span>
            <div><h3>Good stays are on their way.</h3><p>Search Rio de Janeiro or São Paulo to explore our first collection.</p></div>
          </div>
        )}
      </section>
      <section className="editorial-banner page-width">
        <p className="eyebrow">A different kind of travel</p>
        <p className="editorial-quote">“The best journeys leave room for the unexpected.”</p>
        <p className="editorial-credit">A little more time. A little less itinerary.</p>
      </section>
    </>
  );
}
