import { http } from '@/api/http';
import type { Bed, Room } from '@/types';

export interface SaveRoomPayload {
  id?: number;
  floor: number;
  roomNo: string;
  status?: number;
}

export interface SaveBedPayload {
  bedId?: number;
  bedNo?: string;
  status?: string;
}

export function getRooms(includeBeds = false) {
  return http.get<unknown, Room[]>('/rooms', { params: { includeBeds } });
}

export function saveRoom(payload: SaveRoomPayload) {
  return http.post<unknown, Room>('/rooms', payload);
}

export function saveBed(roomId: number, payload: SaveBedPayload) {
  return http.post<unknown, Bed>(`/rooms/${roomId}/beds`, payload);
}
