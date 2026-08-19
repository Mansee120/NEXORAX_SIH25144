import React, { useState } from 'react';

const Header = ({ currentPage, setCurrentPage }) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  const handleNavClick = (page) => {
    setCurrentPage(page);
    setMobileMenuOpen(false);
  };

  return (
    <header>
      <div className="container">
        <div className="header-container">
          <div className="logo" onClick={() => handleNavClick('home')}>
            <img src="/images/MediCrutch_Logo.png" alt="MediCrutch Logo" className="logo-image" />
            <span>MediCrutch</span>
          </div>

          <button className="mobile-menu-btn" onClick={toggleMobileMenu}>
            <i className="fas fa-bars"></i>
          </button>

          <nav>
            <ul className={`nav-menu ${mobileMenuOpen ? 'active' : ''}`}>
              <li>
                <div 
                  className={`nav-link ${currentPage === 'home' ? 'active' : ''}`}
                  onClick={() => handleNavClick('home')}
                >
                  Home
                </div>
              </li>
              <li>
                <div 
                  className={`nav-link ${currentPage === 'about' ? 'active' : ''}`}
                  onClick={() => handleNavClick('about')}
                >
                  About
                </div>
              </li>
              <li>
                <div 
                  className={`nav-link ${currentPage === 'app-link' ? 'active' : ''}`}
                  onClick={() => handleNavClick('app-link')}
                >
                  App Link
                </div>
              </li>
              <li>
                <div 
                  className={`nav-link ${currentPage === 'visualizer' ? 'active' : ''}`}
                  onClick={() => handleNavClick('visualizer')}
                >
                  Visualizer
                </div>
              </li>
              <li>
                <div 
                  className={`nav-link ${currentPage === 'contact' ? 'active' : ''}`}
                  onClick={() => handleNavClick('contact')}
                >
                  Contact Us
                </div>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </header>
  );
};

export default Header;