import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { regenerateDeviceId, getDeviceId } from '../lib/deviceId';

interface DeviceInfo {
  deviceId: string;
}

interface DeviceState {
  deviceInfo: DeviceInfo;
  isInitialized: boolean;
}

interface DeviceActions {
  initializeDevice: () => void;
  regenerateDeviceId: () => void;
  updateDeviceInfo: () => void;
}

type DeviceStore = DeviceState & DeviceActions;

export const useDeviceStore = create<DeviceStore>()(
  persist(
    (set, get) => ({
      // 초기 상태
      deviceInfo: {
        deviceId: 'temp_id',
      },
      isInitialized: false,

      // 디바이스 초기화
      // 디바이스 초기화
      initializeDevice: () => {
        const deviceId = getDeviceId();
        set({
          deviceInfo: { deviceId },
          isInitialized: true,
        });
      },

      // 디바이스 ID 재생성
      regenerateDeviceId: () => {
        const newDeviceId = regenerateDeviceId();
        set({
          deviceInfo: { deviceId: newDeviceId },
        });
      },

      // 디바이스 정보 업데이트
      updateDeviceInfo: () => {
        const deviceId = getDeviceId();
        set({
          deviceInfo: { deviceId },
        });
      },
    }),
    {
      name: 'device-store',
      partialize: (state) => ({
        deviceInfo: { deviceId: state.deviceInfo.deviceId },
        isInitialized: state.isInitialized,
      }),
    }
  )
); 