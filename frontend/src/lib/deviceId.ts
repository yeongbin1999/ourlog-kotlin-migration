// 디바이스 ID 생성 및 관리 유틸리티

const DEVICE_ID_KEY = 'ourlog_device_id';

// 디바이스 ID 생성 함수
const generateDeviceId = (): string => {
  // 브라우저 환경에서 사용 가능한 정보들을 조합하여 고유 ID 생성
  const userAgent = navigator.userAgent;
  const screenResolution = `${screen.width}x${screen.height}`;
  const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const language = navigator.language;
  
  // 위 정보들을 조합하여 해시 생성
  const combinedString = `${userAgent}|${screenResolution}|${timeZone}|${language}`;
  
  // 간단한 해시 함수
  let hash = 0;
  for (let i = 0; i < combinedString.length; i++) {
    const char = combinedString.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // 32bit 정수로 변환
  }
  
  // 16진수로 변환하여 디바이스 ID 생성
  return `device_${Math.abs(hash).toString(16)}_${Date.now().toString(36)}`;
};

// 디바이스 ID 가져오기 (없으면 생성)
export const getDeviceId = (): string => {
  if (typeof window === 'undefined') {
    return 'server_temp_id';
  }
  
  let deviceId = localStorage.getItem(DEVICE_ID_KEY);
  
  if (!deviceId) {
    deviceId = generateDeviceId();
    localStorage.setItem(DEVICE_ID_KEY, deviceId);
  }
  
  return deviceId;
};

// 디바이스 ID 재생성
export const regenerateDeviceId = (): string => {
  if (typeof window === 'undefined') {
    return 'server_temp_id';
  }
  
  const newDeviceId = generateDeviceId();
  localStorage.setItem(DEVICE_ID_KEY, newDeviceId);
  return newDeviceId;
};