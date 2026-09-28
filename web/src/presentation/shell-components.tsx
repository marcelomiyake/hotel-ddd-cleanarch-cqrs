type SiteHeaderProps = {
  readonly onHome: () => void;
  readonly onTrips: () => void;
  readonly current: "stays" | "trips";
};

export function SiteHeader({ onHome, onTrips, current }: SiteHeaderProps) {
  return (
    <header className="site-header">
      <div className="header-inner page-width">
        <button className="brand" type="button" onClick={onHome} aria-label="Wayfarer home">
          <svg className="brand-mark" viewBox="0 0 40 40" aria-hidden="true">
            <path d="M5 19 20 6l15 13v16H5V19Z" /><path d="M14 35V22h12v13M12 15h.01M28 15h.01" />
          </svg>
          <span>WAYFARER<span className="brand-period">.</span></span>
        </button>
        <nav className="primary-nav" aria-label="Main navigation">
          <button className={current === "stays" ? "nav-link active" : "nav-link"} type="button" onClick={onHome}>Find a stay</button>
          <button className={current === "trips" ? "nav-link active" : "nav-link"} type="button" onClick={onTrips}>My trips</button>
        </nav>
        <button className="header-note" type="button" onClick={onTrips}>
          <span className="header-note-icon" aria-hidden="true">↗</span><span>Travel, considered</span>
        </button>
      </div>
    </header>
  );
}

export function Footer() {
  return (
    <footer className="site-footer">
      <div className="page-width footer-inner">
        <span className="footer-brand">WAYFARER<span className="brand-period">.</span></span>
        <p>Independent stays, chosen with care.</p>
        <span>Brazil <span aria-hidden="true">·</span> BRL</span>
      </div>
    </footer>
  );
}

export function ErrorNotice({ message }: { readonly message: string }) {
  if (!message) return null;
  return <div className="global-error page-width" role="alert"><span aria-hidden="true">!</span>{message}</div>;
}
