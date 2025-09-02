export function unwrapList<T = any>(payload: any): T[] {
    if (Array.isArray(payload)) return payload;
    if (Array.isArray(payload?.data?.content)) return payload.data.content;
    if (Array.isArray(payload?.data)) return payload.data;
    if (Array.isArray(payload?.content)) return payload.content;
    return [];
  }