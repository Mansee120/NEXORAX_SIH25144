import React from 'react';

const About = () => {
  return (
    <main>
      <section className="about-hero">
        <div className="container">
          <h1>About MediCrutch</h1>
          <p>Transforming mobility through innovation and technology</p>
        </div>
      </section>

      <section className="about-content">
        <div className="container">
          <div className="about-grid">
            <div className="about-text">
              <h2>Our Mission</h2>
              <p>At MediCrutch, we believe that mobility aids should enhance life, not limit it. Founded in 2020, our team of engineers, designers, and healthcare professionals came together with a shared vision: to revolutionize the crutch experience.</p>
              <p>Traditional crutches often cause discomfort, pain, and even long-term injuries. We set out to change that by combining ergonomic design with smart technology.</p>
              <p>Today, MediCrutch is leading the industry with products that are not only functional but also comfortable, intuitive, and connected.</p>
            </div>
            <div className="about-image">
              <img src="/images/about-image.jpg" alt="MediCrutch Team" />
            </div>
          </div>
        </div>
      </section>

      <section className="team-section">
        <div className="container">
          <h2 className="section-title">Our Leadership Team</h2>
          <p className="section-subtitle">Meet the passionate individuals driving innovation at MediCrutch</p>
          
          <div className="team-grid">
            <div className="team-member">
              <img src="/images/team1.jpg" alt="Dr. Sarah Johnson" />
              <h3>Dr. Sarah Johnson</h3>
              <p>Chief Executive Officer</p>
              <p>Former orthopedic surgeon with 15+ years of experience in mobility solutions.</p>
            </div>
            
            <div className="team-member">
              <img src="/images/team2.jpg" alt="Michael Chen" />
              <h3>Udayraje B</h3>
              <p>Chief Technology Officer</p>
              <p>Expert in IoT and wearable technology with multiple patents in medical devices.</p>
            </div>
            
            <div className="team-member">
              <img src="/images/team3.jpg" alt="Dr. Emily Rodriguez" />
              <h3>Rutuja Hale</h3>
              <p>Head of Product Design</p>
              <p>Biomedical engineer specializing in ergonomic design and user experience.</p>
            </div>
            
            <div className="team-member">
              <img src="/images/team4.jpg" alt="David Kim" />
              <h3>Mrudul Ghadigaonkar</h3>
              <p>Head of Operations</p>
              <p>Supply chain expert with extensive experience in medical device manufacturing.</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
};

export default About;