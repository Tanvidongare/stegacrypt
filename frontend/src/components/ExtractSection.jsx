import React, { useEffect, useState } from 'react';
import { CheckCircle, Copy, Loader, Unlock, Upload, Users, XCircle } from 'lucide-react';
import ImageUpload from './ImageUpload';
import api from '../services/api';
import { parseKeyFile } from '../utils/keyFile';

const MEMBER_REFRESH_EVENT = 'stegacrypt-members-updated';
const MEMBER_REFRESH_MS = 5000;

function ExtractSection() {
  const [extractMode, setExtractMode] = useState('keyfile');
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [privateKey, setPrivateKey] = useState('');
  const [keyFileName, setKeyFileName] = useState('');
  const [members, setMembers] = useState([]);
  const [senderUsername, setSenderUsername] = useState('');
  const [recipientUsername, setRecipientUsername] = useState('');
  const [loading, setLoading] = useState(false);
  const [extractedMessage, setExtractedMessage] = useState('');
  const [extractInfo, setExtractInfo] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    let active = true;

    async function loadMembers() {
      try {
        const result = await api.getSecureChatMembers();
        if (!active) return;

        const loadedMembers = result.members || [];
        setMembers(loadedMembers);
        setSenderUsername((current) => {
          if (current && loadedMembers.some((member) => member.username === current)) {
            return current;
          }
          return loadedMembers[0]?.username || '';
        });
        setRecipientUsername((current) => {
          if (current && loadedMembers.some((member) => member.username === current)) {
            return current;
          }
          return loadedMembers[1]?.username || loadedMembers[0]?.username || '';
        });
      } catch (err) {
        console.error('Failed to load secure chat members:', err);
      }
    }

    const handleRefresh = () => {
      loadMembers();
    };

    loadMembers();
    window.addEventListener(MEMBER_REFRESH_EVENT, handleRefresh);
    const intervalId = window.setInterval(loadMembers, MEMBER_REFRESH_MS);

    return () => {
      active = false;
      window.removeEventListener(MEMBER_REFRESH_EVENT, handleRefresh);
      window.clearInterval(intervalId);
    };
  }, []);

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
    if (extractMode === 'keyfile' && !privateKey.trim()) {
      setError('Please upload the matching key text file');
      return;
    }
    if (extractMode === 'members' && !recipientUsername) {
      setError('Please select the recipient member');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);
    setExtractedMessage('');

    try {
      const result = extractMode === 'members'
        ? await api.extractSharedImage(imageFile, recipientUsername, senderUsername)
        : await api.extractMessage(imageFile, privateKey);

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

      if (extractMode === 'members' && (
        errorMsg.toLowerCase().includes('wrong keys') ||
        errorMsg.toLowerCase().includes('invalid data length') ||
        errorMsg.toLowerCase().includes('tag mismatch') ||
        errorMsg.toLowerCase().includes('unable to authenticate') ||
        errorMsg.toLowerCase().includes('unsupported stego payload format') ||
        errorMsg.toLowerCase().includes('unsupported payload version') ||
        errorMsg.toLowerCase().includes('payload length mismatch') ||
        errorMsg.toLowerCase().includes('invalid payload metadata')
      )) {
        setError("This image doesn't match the selected Secure Chat members. If it was created with a downloaded key file, switch to Key File mode.");
      } else if (errorMsg.toLowerCase().includes('private key')) {
        setError('The uploaded key file does not contain a valid private key.');
      } else if (errorMsg.toLowerCase().includes('wrong private key')) {
        setError('This key file does not match the public key used during embedding.');
      } else if (
        errorMsg.toLowerCase().includes('unsupported stego payload format') ||
        errorMsg.toLowerCase().includes('unsupported payload version') ||
        errorMsg.toLowerCase().includes('payload length mismatch') ||
        errorMsg.toLowerCase().includes('invalid payload metadata')
      ) {
        setError('This image contains a hidden payload, but it does not match the expected extraction format for this mode.');
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
      <h2 className="section-title">Extract Hidden Message</h2>

      <div className="form-group">
        <label>1. Extraction Mode</label>
        <div className="tab-navigation extract-mode-tabs">
          <button
            type="button"
            className={`tab ${extractMode === 'keyfile' ? 'active' : ''}`}
            onClick={() => setExtractMode('keyfile')}
          >
            <Upload size={18} />
            <span>Key File</span>
          </button>
          <button
            type="button"
            className={`tab ${extractMode === 'members' ? 'active' : ''}`}
            onClick={() => setExtractMode('members')}
          >
            <Users size={18} />
            <span>Secure Chat Members</span>
          </button>
        </div>
      </div>

      <div className="form-group">
        <label>2. Select Stego Image</label>
        <ImageUpload onImageSelect={handleImageSelect} preview={imagePreview} />
      </div>

      {extractMode === 'keyfile' ? (
        <div className="form-group">
          <label>3. Upload the Matching Key Text File</label>
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
      ) : (
        <div className="share-user-selectors">
          <div className="form-group">
            <label>3. Sender</label>
            <select className="input" value={senderUsername} onChange={(e) => setSenderUsername(e.target.value)}>
              {members.map((member) => (
                <option key={member.username} value={member.username}>
                  {member.fullName} (@{member.username})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>4. Recipient</label>
            <select className="input" value={recipientUsername} onChange={(e) => setRecipientUsername(e.target.value)}>
              {members.map((member) => (
                <option key={member.username} value={member.username}>
                  {member.fullName} (@{member.username})
                </option>
              ))}
            </select>
          </div>
        </div>
      )}

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
          disabled={loading || !imageFile || (extractMode === 'keyfile' ? !privateKey : !recipientUsername)}
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
            <li>{extractMode === 'members'
              ? 'Select the sender and recipient used in Secure Chat.'
              : 'Upload the key text file downloaded during key generation.'}</li>
            <li>Click Extract Message to decrypt and reveal the hidden text.</li>
          </ol>
          <p className="warning">
            {extractMode === 'members'
              ? 'Use the correct recipient account because extraction depends on the recipient private key.'
              : 'If the key file does not match, the hidden payload cannot be recovered.'}
          </p>
        </div>
      )}
    </div>
  );
}

export default ExtractSection;
