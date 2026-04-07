import React, { useRef, useState } from 'react';
import { Upload, X, Image as ImageIcon } from 'lucide-react';

function ImageUpload({ onImageSelect, preview }) {
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef(null);

  const handleDragEnter = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      handleFile(files[0]);
    }
  };

  const handleFileInput = (e) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      handleFile(files[0]);
    }
  };

  const handleFile = (file) => {
    // Validate file type
    const validTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/bmp'];
    if (!validTypes.includes(file.type)) {
      alert('Please select a valid image file (PNG, JPG, or BMP)');
      return;
    }

    // Validate file size (max 50MB)
    const maxSize = 50 * 1024 * 1024; // 50MB
    if (file.size > maxSize) {
      alert('File is too large! Maximum size is 50MB');
      return;
    }

    onImageSelect(file);
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleRemove = (e) => {
    e.stopPropagation();
    onImageSelect(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div
      className={`image-upload ${isDragging ? 'dragging' : ''} ${preview ? 'has-preview' : ''}`}
      onDragEnter={handleDragEnter}
      onDragLeave={handleDragLeave}
      onDragOver={handleDragOver}
      onDrop={handleDrop}
      onClick={handleClick}
    >
      <input
        ref={fileInputRef}
        type="file"
        accept="image/png,image/jpeg,image/jpg,image/bmp"
        onChange={handleFileInput}
        style={{ display: 'none' }}
      />

      {preview ? (
        <div className="preview-container">
          <img src={preview} alt="Preview" className="image-preview" />
          <button className="remove-btn" onClick={handleRemove}>
            <X size={20} />
          </button>
        </div>
      ) : (
        <div className="upload-placeholder">
          <Upload size={48} />
          <p className="upload-text">
            Drag & drop an image here, or click to browse
          </p>
          <p className="upload-hint">
            Supported formats: PNG, JPG, BMP (max 50MB)
          </p>
        </div>
      )}
    </div>
  );
}

export default ImageUpload;
