import type { APIRequestContext, APIResponse } from '@playwright/test';

interface ApiEnvelope<T> {
  code: number;
  message: string;
  data: T;
}

interface CustomerQueryParams {
  status?: string;
  keyword?: string;
}

interface CustomerLifecyclePayload {
  action: string;
  actionDate: string;
  bedId?: number;
  reason?: string;
}

interface CareRecordQueryParams {
  customerId?: number;
  from?: string;
  to?: string;
}

interface CustomerMealPlanQueryParams {
  weekStartDate?: string;
  customerId?: number;
}

export class ApiClient {
  constructor(
    private readonly request: APIRequestContext,
    private readonly backendBaseUrl: string
  ) {
  }

  private url(path: string): string {
    return `${this.backendBaseUrl}${path}`;
  }

  private async parseOk<T>(response: APIResponse): Promise<T> {
    const body = (await response.json()) as ApiEnvelope<T>;
    if (!response.ok() || body.code !== 0) {
      throw new Error(`API failed: status=${response.status()} code=${body.code} message=${body.message}`);
    }
    return body.data;
  }

  async parseRaw<T>(response: APIResponse): Promise<{ status: number; body: ApiEnvelope<T> }> {
    const body = (await response.json()) as ApiEnvelope<T>;
    return { status: response.status(), body };
  }

  authHeader(token: string): Record<string, string> {
    return {
      Authorization: `Bearer ${token}`
    };
  }

  async login(phone: string, password: string): Promise<{ token: string; user: { id: number; role: string; status: number } }> {
    const response = await this.request.post(this.url('/api/auth/login'), {
      data: { phone, password }
    });
    return this.parseOk(response);
  }

  async register(payload: {
    phone: string;
    password: string;
    realName: string;
    age: number;
    gender: number;
  }): Promise<{ id: number; phone: string }> {
    const response = await this.request.post(this.url('/api/auth/register'), { data: payload });
    return this.parseOk(response);
  }

  async getUsers(token: string) {
    const response = await this.request.get(this.url('/api/users'), {
      headers: this.authHeader(token)
    });
    return this.parseOk<Array<{ id: number; phone: string; realName: string; status: number; role: string }>>(response);
  }

  async updateUserStatus(token: string, id: number, status: number) {
    const response = await this.request.patch(this.url(`/api/users/${id}/status`), {
      headers: this.authHeader(token),
      data: { status }
    });
    return this.parseOk<{ id: number; status: number }>(response);
  }

  async getRooms(token: string, includeBeds = true): Promise<Array<{ id: number; roomNo: string; beds: Array<{ id: number; bedNo: string; status: string }> }>> {
    const response = await this.request.get(this.url('/api/rooms'), {
      headers: this.authHeader(token),
      params: { includeBeds }
    });
    return this.parseOk(response);
  }

  async saveRoom(token: string, payload: { floor: number; roomNo: string; status?: number }) {
    const response = await this.request.post(this.url('/api/rooms'), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk<{ id: number }>(response);
  }

  async saveBed(token: string, roomId: number, payload: { bedId?: number; bedNo?: string; status?: string }) {
    const response = await this.request.post(this.url(`/api/rooms/${roomId}/beds`), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk<{ id: number; status: string }>(response);
  }

  async getCustomers(token: string, params?: CustomerQueryParams) {
    const response = await this.request.get(this.url('/api/customers'), {
      headers: this.authHeader(token),
      params
    });
    return this.parseOk<Array<{ id: number; name: string; status: string; bedId?: number; bedNo?: string; roomNo?: string }>>(response);
  }

  async createCustomer(token: string, payload: { name: string; phone?: string; age: number; gender: number; note?: string }) {
    const response = await this.request.post(this.url('/api/customers'), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk<{ id: number; name: string; status: string }>(response);
  }

  async updateCustomerLifecycle(token: string, customerId: number, payload: CustomerLifecyclePayload) {
    const response = await this.request.patch(this.url(`/api/customers/${customerId}/lifecycle`), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk<{ id: number; status: string; bedId?: number }>(response);
  }

  async saveWeeklyMenu(token: string, weekStartDate: string, payload: Record<string, string>) {
    const response = await this.request.put(this.url(`/api/meals/weekly-menus/${weekStartDate}`), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk(response);
  }

  async getWeeklyMenu(token: string, weekStartDate: string) {
    const response = await this.request.get(this.url(`/api/meals/weekly-menus/${weekStartDate}`), {
      headers: this.authHeader(token)
    });
    return this.parseOk<Record<string, string>>(response);
  }

  async saveCustomerMealPlan(
    token: string,
    customerId: number,
    weekStartDate: string,
    payload: { mealType: string; dietTaboo?: string; note?: string }
  ) {
    const response = await this.request.put(this.url(`/api/meals/customer-plans/${customerId}/${weekStartDate}`), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk(response);
  }

  async getCustomerMealPlan(token: string, customerId: number, weekStartDate: string) {
    const response = await this.request.get(this.url(`/api/meals/customer-plans/${customerId}/${weekStartDate}`), {
      headers: this.authHeader(token)
    });
    return this.parseOk<{
      id?: number;
      customerId: number;
      customerName?: string;
      weekStartDate: string;
      mealType: string;
      dietTaboo?: string;
      note?: string;
      createdBy?: number;
    }>(response);
  }

  async listCustomerMealPlans(token: string, params?: CustomerMealPlanQueryParams) {
    const response = await this.request.get(this.url('/api/meals/customer-plans'), {
      headers: this.authHeader(token),
      params
    });
    return this.parseOk<Array<{ customerId: number; customerName?: string; mealType: string; dietTaboo?: string; note?: string }>>(response);
  }

  async getCareLevels(token: string) {
    const response = await this.request.get(this.url('/api/care-levels'), {
      headers: this.authHeader(token)
    });
    return this.parseOk<Array<{ id: number; name: string; status: number }>>(response);
  }

  async createCareLevel(token: string, payload: { name: string; description?: string; status?: number }) {
    const response = await this.request.post(this.url('/api/care-levels'), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk<{ id: number; name: string; status: number }>(response);
  }

  async createCareRecord(token: string, payload: { customerId: number; careDate: string; content: string }) {
    const response = await this.request.post(this.url('/api/care-records'), {
      headers: this.authHeader(token),
      data: payload
    });
    return this.parseOk<{ id: number; customerId: number }>(response);
  }

  async getCareRecords(token: string, params?: CareRecordQueryParams) {
    const response = await this.request.get(this.url('/api/care-records'), {
      headers: this.authHeader(token),
      params
    });
    return this.parseOk<Array<{ id: number; customerId: number; customerName?: string; careDate: string; content: string; performerName?: string }>>(response);
  }

  async rawGet(path: string, token: string | null, params?: Record<string, unknown>) {
    const response = await this.request.get(this.url(path), {
      headers: token ? this.authHeader(token) : undefined,
      params
    });
    return this.parseRaw(response);
  }

  async rawPatch(path: string, token: string | null, data: Record<string, unknown>) {
    const response = await this.request.patch(this.url(path), {
      headers: token ? this.authHeader(token) : undefined,
      data
    });
    return this.parseRaw(response);
  }

  async rawPut(path: string, token: string | null, data: Record<string, unknown>) {
    const response = await this.request.put(this.url(path), {
      headers: token ? this.authHeader(token) : undefined,
      data
    });
    return this.parseRaw(response);
  }

  async rawPost(path: string, token: string | null, data: Record<string, unknown>) {
    const response = await this.request.post(this.url(path), {
      headers: token ? this.authHeader(token) : undefined,
      data
    });
    return this.parseRaw(response);
  }
}
