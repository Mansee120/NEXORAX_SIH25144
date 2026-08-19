import React from 'react';

const Footer = ({ setCurrentPage }) => {
  return (
    <footer>
      <div className="container">
        <div className="footer-content">
          <div className="footer-column">
            <div className="footer-logo">
              <img src="/images/MediCrutch_Logo.png" alt="MediCrutch Logo" className="logo-image" />
              <span>MediCrutch</span>
            </div>
            <p>Revolutionizing mobility with smart technology. Our innovative crutches provide comfort, stability, and connectivity for enhanced recovery.</p>
            <div className="social-links">
              <a href="#"><i className="fab fa-facebook-f"></i></a>
              <a href="#"><i className="fab fa-twitter"></i></a>
              <a href="#"><i className="fab fa-instagram"></i></a>
              <a href="#"><i className="fab fa-linkedin-in"></i></a>
            </div>
          </div>
          
          <div className="footer-column">
            <h3>Quick Links</h3>
            <ul className="footer-links">
              <li><div onClick={() => setCurrentPage('home')}>Home</div></li>
              <li><div onClick={() => setCurrentPage('about')}>About Us</div></li>
              <li><div onClick={() => setCurrentPage('app-link')}>Our Apps</div></li>
              <li><div onClick={() => setCurrentPage('visualizer')}>Visualizer</div></li>
              <li><div onClick={() => setCurrentPage('contact')}>Contact</div></li>
            </ul>
          </div>
          
          <div className="footer-column">
            <h3>Our Apps</h3>
            <ul className="footer-links">
              <li><a href="#">StrideX - Mobility Tracker</a></li>
              <li><a href="#">MediKart - Accessories</a></li>
              <li><a href="#">Crutch Visualizer</a></li>
              <li><a href="#">Health Dashboard</a></li>
            </ul>
          </div>
          
          <div className="footer-column">
            <h3>Support</h3>
            <ul className="footer-links">
              <li><a href="#">Help Center</a></li>
              <li><a href="#">User Manuals</a></li>
              <li><a href="#">Warranty</a></li>
              <li><a href="#">Contact Support</a></li>
            </ul>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p>&copy; 2023 MediCrutch. All rights reserved. | <a href="#">Privacy Policy</a> | <a href="#">Terms of Service</a></p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;