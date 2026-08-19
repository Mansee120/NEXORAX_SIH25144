import React, { useState } from 'react';

const Visualizer = () => {
  const [selectedColor, setSelectedColor] = useState('#2a7de1');
  const [selectedSize, setSelectedSize] = useState('medium');
  const [accessories, setAccessories] = useState([]);

  const colors = [
    { name: 'Blue', value: '#2a7de1' },
    { name: 'Black', value: '#1a1a2e' },
    { name: 'White', value: '#ffffff' },
    { name: 'Red', value: '#e74c3c' },
    { name: 'Green', value: '#34c759' }
  ];

  const sizes = [
    { name: 'Small', value: 'small' },
    { name: 'Medium', value: 'medium' },
    { name: 'Large', value: 'large' },
    { name: 'X-Large', value: 'xlarge' }
  ];

  const accessoryOptions = [
    { id: 1, name: 'Comfort Grip', icon: 'fas fa-hand-holding' },
    { id: 2, name: 'LED Lights', icon: 'fas fa-lightbulb' },
    { id: 3, name: 'Phone Holder', icon: 'fas fa-mobile-alt' },
    { id: 4, name: 'Water Bottle', icon: 'fas fa-wine-bottle' }
  ];

  const toggleAccessory = (accessoryId) => {
    if (accessories.includes(accessoryId)) {
      setAccessories(accessories.filter(id => id !== accessoryId));
    } else {
      setAccessories([...accessories, accessoryId]);
    }
  };

  return (
    <main>
      <section className="visualizer-hero">
        <div className="container">
          <h1>Crutch Visualizer</h1>
          <p>Customize your MediCrutch to match your style and needs</p>
        </div>
      </section>

      <section className="visualizer-section">
        <div className="container">
          <div className="visualizer-container">
            <div className="visualizer-controls">
              <div className="control-group">
                <h3>Select Color</h3>
                <div className="color-options">
                  {colors.map(color => (
                    <div 
                      key={color.value}
                      className={`color-option ${selectedColor === color.value ? 'active' : ''}`}
                      style={{ backgroundColor: color.value, border: color.value === '#ffffff' ? '1px solid #ddd' : 'none' }}
                      onClick={() => setSelectedColor(color.value)}
                      title={color.name}
                    ></div>
                  ))}
                </div>
              </div>

              <div className="control-group">
                <h3>Select Size</h3>
                <div className="size-options">
                  {sizes.map(size => (
                    <div 
                      key={size.value}
                      className={`size-option ${selectedSize === size.value ? 'active' : ''}`}
                      onClick={() => setSelectedSize(size.value)}
                    >
                      {size.name}
                    </div>
                  ))}
                </div>
              </div>

              <div className="control-group">
                <h3>Add Accessories</h3>
                <div className="accessory-options">
                  {accessoryOptions.map(accessory => (
                    <div 
                      key={accessory.id}
                      className={`accessory-option ${accessories.includes(accessory.id) ? 'active' : ''}`}
                      onClick={() => toggleAccessory(accessory.id)}
                    >
                      <i className={accessory.icon}></i>
                      <span>{accessory.name}</span>
                    </div>
                  ))}
                </div>
              </div>

              <button className="btn btn-primary" style={{width: '100%', marginTop: '20px'}}>
                Add to Cart - $299.99
              </button>
            </div>

            <div className="visualizer-display">
              <div className="crutch-model" style={{ color: selectedColor }}>
                <i className="fas fa-crutch"></i>
              </div>
              {accessories.includes(1) && (
                <div className="accessory-preview comfort-grip" style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }}>
                  <i className="fas fa-hand-holding" style={{ fontSize: '2rem', color: '#8B4513' }}></i>
                </div>
              )}
              {accessories.includes(2) && (
                <div className="accessory-preview led-lights" style={{ position: 'absolute', bottom: '20%', left: '50%', transform: 'translateX(-50%)' }}>
                  <i className="fas fa-lightbulb" style={{ fontSize: '1.5rem', color: '#FFD700' }}></i>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>
    </main>
  );
};

export default Visualizer;