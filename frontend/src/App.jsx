import React, { useState } from 'react';
import { AlertCircle, Github, KeyRound, Lock, Shield, Unlock } from 'lucide-react';
import EmbedSection from './components/EmbedSection';
import ExtractSection from './components/ExtractSection';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('embed');

  return (
    <div className="app">
      <header className="header">
        <div className="header-content">
          <div className="logo">
            <Shield size={32} />
            <h1>StegaCrypt</h1>
          </div>
          <p className="tagline">Public-Key Steganography with Hybrid RSA and AES Encryption</p>
        </div>
      </header>

      <main className="main-content">
        <div className="tab-navigation">
          <button
            className={`tab ${activeTab === 'embed' ? 'active' : ''}`}
            onClick={() => setActiveTab('embed')}
          >
            <Lock size={20} />
            <span>Embed Message</span>
          </button>
          <button
            className={`tab ${activeTab === 'extract' ? 'active' : ''}`}
            onClick={() => setActiveTab('extract')}
          >
            <Unlock size={20} />
            <span>Extract Message</span>
          </button>
        </div>

        <div className="tab-content">
          {activeTab === 'embed' ? <EmbedSection /> : <ExtractSection />}
        </div>

        <div className="info-section">
          <div className="info-card">
            <AlertCircle size={20} />
            <div>
              <h3>Security Features</h3>
              <ul>
                <li>RSA key-file workflow without visible key text</li>
                <li>AES-256-GCM session encryption for the message payload</li>
                <li>OAEP-based RSA wrapping for the AES session key</li>
                <li>PRNG-based randomized pixel selection tied to the key pair</li>
              </ul>
            </div>
          </div>

          <div className="info-card accent-card">
            <KeyRound size={20} />
            <div>
              <h3>How It Works</h3>
              <ul>
                <li>Generate a compact key text file before embedding.</li>
                <li>Upload the same key file during extraction.</li>
                <li>Keep the stego image and key file separate until recovery.</li>
              </ul>
            </div>
          </div>
        </div>
      </main>

      <footer className="footer">
        <p>StegaCrypt v2.0 - MIT-WPU Mini Project 2025-2026</p>
        <div className="footer-links">
          <a href="https://github.com" target="_blank" rel="noopener noreferrer">
            <Github size={20} />
            <span>GitHub</span>
          </a>
        </div>
      </footer>
    </div>
  );
}

export default App;

