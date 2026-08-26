export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface UserInfo {
  id: number;
  phone: string;
  realName: string;
  age: number;
  gender: number;
  role: 'ADMIN' | 'STAFF';
  status: number;
}

export interface LoginResponse {
  token: string;
  expireAt: string;
  user: UserInfo;
}

export interface Customer {
  id: number;
  name: string;
  phone?: string;
  age: number;
  gender: number;
  status: 'DRAFT' | 'RESIDENT' | 'OUTING' | 'DISCHARGED';
  bedId?: number;
  bedNo?: string;
  roomNo?: string;
  checkinDate?: string;
  note?: string;
}

export interface Bed {
  id: number;
  roomId: number;
  roomNo: string;
  bedNo: string;
  status: 'AVAILABLE' | 'OCCUPIED' | 'DISABLED';
  customerId?: number;
  customerName?: string;
}

export interface Room {
  id: number;
  floor: number;
  roomNo: string;
  status: number;
  beds: Bed[];
}

export interface WeeklyMenu {
  weekStartDate: string;
  mon: string;
  tue: string;
  wed: string;
  thu: string;
  fri: string;
  sat: string;
  sun: string;
}

export interface CustomerMealPlan {
  id?: number;
  customerId: number;
  customerName?: string;
  weekStartDate: string;
  mealType: string;
  dietTaboo?: string;
  note?: string;
  createdBy?: number;
}

export interface CareLevel {
  id: number;
  name: string;
  description?: string;
  status: number;
}

export interface CareRecord {
  id: number;
  customerId: number;
  customerName?: string;
  careDate: string;
  content: string;
  performedBy: number;
  performerName?: string;
}

export interface ServiceObjectRelation {
  customerId: number;
  customerName: string;
  managerUserId?: number;
  managerName?: string;
  managerPhone?: string;
  assignedAt?: string;
}

export interface ServiceFocusRecord {
  id: number;
  customerId: number;
  customerName?: string;
  serviceName: string;
  purchaseDate: string;
  expireDate?: string;
  serviceStatus: 'ACTIVE' | 'PAUSED' | 'ENDED';
  note?: string;
  createdBy: number;
  createdByName?: string;
  createdAt: string;
}
