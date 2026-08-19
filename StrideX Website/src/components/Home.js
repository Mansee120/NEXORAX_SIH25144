import React from 'react';
import { useScrollAnimation } from '../hooks/useScrollAnimation';

const Home = () => {
  const [featuresRef, featuresVisible] = useScrollAnimation();
  const [videoRef, videoVisible] = useScrollAnimation();

  return (
    <>
      <section className="hero">
        <div className="container">
          <h1 className="typewriter">MediCrutch</h1>
          <p>Revolutionizing mobility with smart crutch technology. Experience comfort, stability, and innovation in every step.</p>
          <div className="hero-btns">
            <div className="btn btn-primary pulse">Download App</div>
            <div className="btn btn-secondary">Try Visualizer</div>
          </div>
        </div>
      </section>

      <section ref={videoRef} className={`video-section ${videoVisible ? 'revealed' : ''}`}>
        <div className="container">
          <h2 className="section-title">Experience Our 3D Crutch Model</h2>
          <p className="section-subtitle">Watch how our innovative design provides superior comfort and support compared to traditional crutches.</p>
          <div className="video-container scroll-reveal">
            <video controls poster="/images/crutch-poster.jpg">
              <source src="/videos/3d-model.mp4" type="video/mp4" />
              Your browser does not support the video tag.
            </video>
          </div>
        </div>
      </section>

      <section ref={featuresRef} className={`features ${featuresVisible ? 'revealed' : ''}`}>
        <div className="container">
          <h2 className="section-title">Why Choose MediCrutch?</h2>
          <p className="section-subtitle">Our smart crutches are designed with cutting-edge technology to enhance your mobility experience.</p>
          
          <div className="features-grid">
            <div className="feature-card scroll-reveal" style={{animationDelay: '0.1s'}}>
              <div className="feature-icon">
                <i className="fas fa-heartbeat"></i>
              </div>
              <h3>Health Monitoring</h3>
              <p>Track your recovery progress with built-in sensors that monitor weight distribution and usage patterns.</p>
            </div>
            
            <div className="feature-card scroll-reveal" style={{animationDelay: '0.2s'}}>
              <div className="feature-icon">
                <i className="fas fa-shield-alt"></i>
              </div>
              <h3>Enhanced Safety</h3>
              <p>Anti-slip technology and stability alerts ensure you stay secure on any surface.</p>
            </div>
            
            <div className="feature-card scroll-reveal" style={{animationDelay: '0.3s'}}>
              <div className="feature-icon">
                <i className="fas fa-battery-full"></i>
              </div>
              <h3>Long Battery Life</h3>
              <p>Up to 30 days of usage on a single charge with our efficient power management system.</p>
            </div>
            
            <div className="feature-card scroll-reveal" style={{animationDelay: '0.4s'}}>
              <div className="feature-icon">
                <i className="fas fa-mobile-alt"></i>
              </div>
              <h3>Smart App Integration</h3>
              <p>Connect to our mobile app for personalized settings, progress tracking, and support.</p>
            </div>
          </div>
        </div>
      </section>
    </>
  );
};

export default Home;