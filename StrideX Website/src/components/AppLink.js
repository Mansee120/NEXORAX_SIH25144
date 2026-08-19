import React from 'react';

const AppLink = () => {
  return (
    <main>
      <section className="apps-section">
        <div className="container">
          <h1 className="section-title">Download Our Apps</h1>
          <p className="section-subtitle">Enhance your MediCrutch experience with our companion applications</p>
          
          <div className="apps-grid">
            <div className="app-card">
              <div className="app-icon">
                <i className="fas fa-walking"></i>
              </div>
              <h3>StrideX</h3>
              <p>Track your recovery progress, set goals, and get personalized recommendations with our advanced mobility tracking app.</p>
              <a href="#" className="app-badge">
                <i className="fab fa-google-play"></i> Google Play
              </a>
            </div>
            
            <div className="app-card">
              <div className="app-icon">
                <i className="fas fa-shopping-cart"></i>
              </div>
              <h3>MediKart</h3>
              <p>Shop for MediCrutch accessories, replacement parts, and complementary products with exclusive app-only discounts.</p>
              <a href="#" className="app-badge">
                <i className="fab fa-app-store"></i> App Store
              </a>
            </div>
          </div>
          
          <div style={{marginTop: '60px', textAlign: 'center'}}>
            <h2>App Features</h2>
            <div className="features-grid" style={{marginTop: '30px'}}>
              <div className="feature-card">
                <h3>Progress Tracking</h3>
                <p>Monitor your weight-bearing progress and recovery milestones with detailed analytics.</p>
              </div>
              
              <div className="feature-card">
                <h3>Personalized Settings</h3>
                <p>Customize your crutch settings for optimal comfort and support based on your specific needs.</p>
              </div>
              
              <div className="feature-card">
                <h3>Emergency Alerts</h3>
                <p>Get immediate assistance with our fall detection and emergency contact notification system.</p>
              </div>
              
              <div className="feature-card">
                <h3>Community Support</h3>
                <p>Connect with other users, share experiences, and get advice from healthcare professionals.</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
};

export default AppLink;