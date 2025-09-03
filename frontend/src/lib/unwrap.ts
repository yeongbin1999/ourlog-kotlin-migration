import type { AxiosResponse } from "axios";

export async function unwrap<T>(promise: Promise<AxiosResponse<T>>): Promise<T> {
  const { data } = await promise;
  return data;
}

export function unwrapList<T>(input: unknown): T[] {
  if (Array.isArray(input)) return input as T[];
  if (
    typeof input === "object" &&
    input !== null &&
    Array.isArray((input as { data?: unknown }).data)
  ) {
    return (input as { data: T[] }).data;
  }
  return [];
}
