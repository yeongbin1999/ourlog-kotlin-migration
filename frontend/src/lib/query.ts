export function buildQuery(params: Record<string, string | number | boolean | undefined | null>) {
    const sp = new URLSearchParams();
    for (const [k, v] of Object.entries(params)) {
      if (v === undefined || v === null) continue;
      sp.append(k, String(v));
    }
    return sp.toString();
  }
  
  export function pageParams({ page = 0, size = 10, sort }: { page?: number; size?: number; sort?: string } = {}) {
    return buildQuery({ page, size, sort });
  }
  