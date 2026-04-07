const KEY_FILE_VERSION = 1;
const KEY_FILE_ALGORITHM = 'RSA-OAEP-2048/AES-256-GCM';

export function buildKeyFile(publicKey, privateKey) {
  return JSON.stringify({
    v: KEY_FILE_VERSION,
    alg: KEY_FILE_ALGORITHM,
    pub: publicKey,
    priv: privateKey,
  });
}

export function downloadKeyFile(publicKey, privateKey) {
  const blob = new Blob([buildKeyFile(publicKey, privateKey)], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = url;
  link.download = `stegacrypt_key_${Date.now()}.txt`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export function parseKeyFile(text) {
  const trimmed = text.trim();

  if (!trimmed) {
    throw new Error('The selected key file is empty.');
  }

  if (trimmed.startsWith('{')) {
    const parsed = JSON.parse(trimmed);
    const publicKey = parsed.pub || parsed.publicKey || '';
    const privateKey = parsed.priv || parsed.privateKey || '';

    if (!privateKey) {
      throw new Error('This key file does not contain a private key.');
    }

    return { publicKey, privateKey };
  }

  if (trimmed.includes('-----BEGIN PRIVATE KEY-----')) {
    return { publicKey: '', privateKey: trimmed };
  }

  throw new Error('Please upload the StegaCrypt key text file that was downloaded during generation.');
}
