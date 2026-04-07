import React, { useMemo, useState } from 'react';
import { CheckCircle, Download, KeyRound, Loader, Lock, Sparkles, XCircle } from 'lucide-react';
import ImageUpload from './ImageUpload';
import api from '../services/api';
import { downloadKeyFile } from '../utils/keyFile';

function EmbedSection() {
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [message, setMessage] = useState('');
  const [publicKey, setPublicKey] = useState('');
  const [keyFileReady, setKeyFileReady] = useState(false);
  const [useCompression, setUseCompression] = useState(true);
  const [loading, setLoading] = useState(false);
  const [generatingKeys, setGeneratingKeys] = useState(false);
  const [stegoImage, setStegoImage] = useState(null);
  const [capacity, setCapacity] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const estimatedPayloadBytes = useMemo(() => {
    const messageBytes = new TextEncoder().encode(message).length;
    const estimatedMessageBytes = useCompression && messageBytes > 100
      ? Math.max(Math.round(messageBytes * 0.8), 32)
      : messageBytes;

    return estimatedMessageBytes + 320;
  }, [message, useCompression]);

  const handleImageSelect = async (file) => {
    setImageFile(file);
    setError(null);
    setStegoImage(null);
    setSuccess(false);

    const reader = new FileReader();
    reader.onload = (e) => setImagePreview(e.target.result);
    reader.readAsDataURL(file);

    try {
      const capacityData = await api.checkCapacity(file);
      setCapacity(capacityData);
    } catch (err) {
      console.error('Failed to check capacity:', err);
    }
  };

  const handleGenerateKeys = async () => {
    setGeneratingKeys(true);
    setError(null);
    setKeyFileReady(false);

    try {
      const result = await api.generateKeyPair();
      const generatedPublicKey = result.publicKey || '';
      const generatedPrivateKey = result.privateKey || '';

      if (!generatedPublicKey || !generatedPrivateKey) {
        throw new Error('The server did not return a complete key pair.');
      }

      setPublicKey(generatedPublicKey);
      downloadKeyFile(generatedPublicKey, generatedPrivateKey);
      setKeyFileReady(true);
    } catch (err) {
      console.error('Key generation error:', err);
      setError(await api.getErrorMessage(err, err.message || 'Failed to generate RSA key pair.'));
    } finally {
      setGeneratingKeys(false);
    }
  };

  const handleEmbed = async () => {
    if (!imageFile) {
      setError('Please select an image');
      return;
    }
    if (!message.trim()) {
      setError('Please enter a message');
      return;
    }
    if (!publicKey.trim()) {
      setError('Please generate and download a key file before embedding');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const stegoBlob = await api.embedMessage(imageFile, message, publicKey, useCompression);
      const url = URL.createObjectURL(stegoBlob);
      setStegoImage(url);
      setSuccess(true);
    } catch (err) {
      console.error('Embed error:', err);
      setError(await api.getErrorMessage(err, 'Failed to embed message. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = () => {
    if (!stegoImage) return;

    const link = document.createElement('a');
    link.href = stegoImage;
    link.download = `stego_${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleReset = () => {
    setImageFile(null);
    setImagePreview(null);
    setMessage('');
    setPublicKey('');
    setKeyFileReady(false);
    setStegoImage(null);
    setCapacity(null);
    setError(null);
    setSuccess(false);
  };

  return (
    <div className="section">
      <h2 className="section-title">Hide Secret Message with a Downloaded Key File</h2>

      <div className="form-group">
        <label>1. Select Carrier Image</label>
        <ImageUpload onImageSelect={handleImageSelect} preview={imagePreview} />
        {capacity && (
          <div className="capacity-info">
            <p>Image: {capacity.width}x{capacity.height} pixels</p>
            <p>Max Capacity: ~{capacity.capacityKB?.toFixed(2)} KB</p>
            <p>Estimated Payload: ~{(estimatedPayloadBytes / 1024).toFixed(2)} KB</p>
          </div>
        )}
      </div>

      <div className="form-group">
        <label>2. Enter Secret Message</label>
        <textarea
          className="textarea"
          placeholder="Type your secret message here..."
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          rows={6}
          disabled={loading}
        />
        <div className="char-count">
          {message.length} characters
          {capacity && message.length > 0 && (
            <span className={estimatedPayloadBytes > capacity.capacityBytes ? 'warning' : 'success'}>
              {' '}(~{(estimatedPayloadBytes / 1024).toFixed(2)} KB after encryption overhead)
            </span>
          )}
        </div>
      </div>

      <div className="form-group">
        <div className="inline-label-row">
          <label>3. Generate Key File</label>
          <button className="btn btn-secondary btn-small" onClick={handleGenerateKeys} disabled={generatingKeys || loading}>
            {generatingKeys ? <Loader size={16} className="spin" /> : <Sparkles size={16} />}
            <span>{generatingKeys ? 'Generating...' : 'Generate & Download'}</span>
          </button>
        </div>

        <div className="key-file-card">
          <div className="key-file-icon">
            <KeyRound size={26} />
          </div>
          <div>
            <h3>{keyFileReady ? 'Key file downloaded' : 'No key file generated yet'}</h3>
            <p className="field-hint">
              {keyFileReady
                ? 'The key is not displayed in the browser. Use the downloaded text file when extracting the hidden message.'
                : 'Generate once before embedding. A compact text file containing the matching key pair will download automatically.'}
            </p>
          </div>
        </div>
      </div>

      <div className="form-group">
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={useCompression}
            onChange={(e) => setUseCompression(e.target.checked)}
            disabled={loading}
          />
          <span>Enable GZIP compression before encryption</span>
        </label>
      </div>

      {error && (
        <div className="alert alert-error">
          <XCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <CheckCircle size={20} />
          <span>Message embedded successfully. Download the stego image and keep the key text file safe.</span>
        </div>
      )}

      <div className="button-group">
        <button
          className="btn btn-primary"
          onClick={handleEmbed}
          disabled={loading || !imageFile || !message || !publicKey}
        >
          {loading ? (
            <>
              <Loader size={20} className="spin" />
              <span>Embedding...</span>
            </>
          ) : (
            <>
              <Lock size={20} />
              <span>Embed Message</span>
            </>
          )}
        </button>

        {stegoImage && (
          <button className="btn btn-success" onClick={handleDownload}>
            <Download size={20} />
            <span>Download Stego Image</span>
          </button>
        )}

        <button className="btn btn-secondary" onClick={handleReset}>
          Reset
        </button>
      </div>

      {stegoImage && (
        <div className="result-preview">
          <h3>Stego Image Preview</h3>
          <div className="image-comparison">
            <div className="image-container">
              <p>Original</p>
              <img src={imagePreview} alt="Original" />
            </div>
            <div className="image-container">
              <p>Stego Output</p>
              <img src={stegoImage} alt="Stego" />
            </div>
          </div>
          <p className="hint">The embedded image looks the same, but only the downloaded key file can recover the hidden message.</p>
        </div>
      )}

      <div className="info-box subtle-box">
        <h4>Hybrid Encryption Flow</h4>
        <ol>
          <li>Your message is optionally compressed.</li>
          <li>A random AES-256 session key encrypts the message.</li>
          <li>The AES session key is encrypted with the generated RSA public key.</li>
          <li>The final payload is hidden inside the image using randomized LSB positions.</li>
        </ol>
      </div>
    </div>
  );
}

export default EmbedSection;
