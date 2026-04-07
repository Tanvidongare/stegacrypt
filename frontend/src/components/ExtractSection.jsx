import React, { useState } from 'react';
import { CheckCircle, Copy, Loader, Unlock, Upload, XCircle } from 'lucide-react';
import ImageUpload from './ImageUpload';
import api from '../services/api';
import { parseKeyFile } from '../utils/keyFile';

function ExtractSection() {
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [privateKey, setPrivateKey] = useState('');
  const [keyFileName, setKeyFileName] = useState('');
  const [loading, setLoading] = useState(false);
  const [extractedMessage, setExtractedMessage] = useState('');
  const [extractInfo, setExtractInfo] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleImageSelect = (file) => {
    setImageFile(file);
    setError(null);
    setExtractedMessage('');
    setSuccess(false);
    setCopied(false);

    const reader = new FileReader();
    reader.onload = (e) => setImagePreview(e.target.result);
    reader.readAsDataURL(file);
  };

  const handleKeyFileSelect = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setError(null);
    setPrivateKey('');
    setKeyFileName('');
    setExtractedMessage('');
    setExtractInfo(null);
    setSuccess(false);

    try {
      const text = await file.text();
      const parsed = parseKeyFile(text);
      setPrivateKey(parsed.privateKey);
      setKeyFileName(file.name);
    } catch (err) {
      console.error('Key file error:', err);
      setError(err.message || 'Could not read the selected key file.');
    } finally {
      event.target.value = '';
    }
  };

  const handleExtract = async () => {
    if (!imageFile) {
      setError('Please select a stego image');
      return;
    }
    if (!privateKey.trim()) {
      setError('Please upload the matching key text file');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);
    setExtractedMessage('');

    try {
      const result = await api.extractMessage(imageFile, privateKey);

      if (result.success) {
        setExtractedMessage(result.message);
        setExtractInfo({
          encryptedSize: result.encryptedSize,
          messageLength: result.messageLength,
          wrappedKeyLength: result.wrappedKeyLength,
          usedCompression: result.usedCompression,
          encryptionMode: result.encryptionMode,
        });
        setSuccess(true);
      } else {
        setError(result.message || 'Failed to extract message');
      }
    } catch (err) {
      console.error('Extract error:', err);
      const errorMsg = await api.getErrorMessage(err, 'Failed to extract message');

      if (errorMsg.toLowerCase().includes('private key')) {
        setError('The uploaded key file does not contain a valid private key.');
      } else if (errorMsg.toLowerCase().includes('wrong private key')) {
        setError('This key file does not match the public key used during embedding.');
      } else if (errorMsg.toLowerCase().includes('invalid data length')) {
        setError('This image does not contain a readable hidden payload, or it has been altered.');
      } else {
        setError(errorMsg);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    if (!extractedMessage) return;
    await navigator.clipboard.writeText(extractedMessage);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleReset = () => {
    setImageFile(null);
    setImagePreview(null);
    setPrivateKey('');
    setKeyFileName('');
    setExtractedMessage('');
    setExtractInfo(null);
    setError(null);
    setSuccess(false);
    setCopied(false);
  };

  return (
    <div className="section">
      <h2 className="section-title">Extract Hidden Message with the Key File</h2>

      <div className="form-group">
        <label>1. Select Stego Image</label>
        <ImageUpload onImageSelect={handleImageSelect} preview={imagePreview} />
      </div>

      <div className="form-group">
        <label>2. Upload the Matching Key Text File</label>
        <label className={`key-file-upload ${privateKey ? 'has-file' : ''}`}>
          <input
            type="file"
            accept=".txt,.pem,.key,application/json,text/plain"
            onChange={handleKeyFileSelect}
            disabled={loading}
          />
          <Upload size={24} />
          <span>{keyFileName || 'Choose downloaded StegaCrypt key file'}</span>
        </label>
        <p className="field-hint">The private key is read from the file for extraction and is not displayed on the page.</p>
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
          <span>Message extracted successfully.</span>
        </div>
      )}

      <div className="button-group">
        <button
          className="btn btn-primary"
          onClick={handleExtract}
          disabled={loading || !imageFile || !privateKey}
        >
          {loading ? (
            <>
              <Loader size={20} className="spin" />
              <span>Extracting...</span>
            </>
          ) : (
            <>
              <Unlock size={20} />
              <span>Extract Message</span>
            </>
          )}
        </button>

        <button className="btn btn-secondary" onClick={handleReset}>
          Reset
        </button>
      </div>

      {extractedMessage && (
        <div className="result-section">
          <div className="result-header">
            <h3>Extracted Message</h3>
            <button className="btn btn-small" onClick={handleCopy}>
              <Copy size={16} />
              <span>{copied ? 'Copied!' : 'Copy'}</span>
            </button>
          </div>

          <div className="message-box">
            <p>{extractedMessage}</p>
          </div>

          {extractInfo && (
            <div className="extract-info">
              <p>Message Length: {extractInfo.messageLength} characters</p>
              <p>Embedded Payload Size: {extractInfo.encryptedSize} bytes</p>
              <p>RSA Wrapped Session Key: {extractInfo.wrappedKeyLength} bytes</p>
              <p>Compression Used: {extractInfo.usedCompression ? 'Yes' : 'No'}</p>
              <p>Crypto Mode: {extractInfo.encryptionMode}</p>
            </div>
          )}
        </div>
      )}

      {!extractedMessage && !error && (
        <div className="info-box">
          <h4>How to Extract</h4>
          <ol>
            <li>Upload the stego image created during embedding.</li>
            <li>Upload the key text file downloaded during key generation.</li>
            <li>Click Extract Message to decrypt and reveal the hidden text.</li>
          </ol>
          <p className="warning">If the key file does not match, the hidden payload cannot be recovered.</p>
        </div>
      )}
    </div>
  );
}

export default ExtractSection;
