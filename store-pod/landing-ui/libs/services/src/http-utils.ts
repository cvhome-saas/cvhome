export function handleResponse<T>(it: Response): T | undefined {
    if (it.ok) {
        return it.json() as unknown as T;
    } else {
        console.error(it);
        throw new Error(it.statusText);
    }
}

function buildHeader<T>(method: string, it?: T) {
    const accessToken = typeof window !== 'undefined' ? sessionStorage.getItem('access_token') : undefined;
    const headers: Record<string, string> = {};
    if (it) {
        headers["Content-Type"] = "application/json";
    }
    if (accessToken) {
        headers["Authorization"] = `Bearer ${accessToken}`;
    }
    const result: any = {
        method: method,
        headers
    };
    if (it) {
        result.body = JSON.stringify(it);
    }
    return result;
}

export function post<T>(it: T) {
    return buildHeader('POST', it);
}

export function put<T>(it: T) {
    return buildHeader('PUT', it);
}

export function del() {
    return buildHeader('DELETE');
}

export function get() {
    return buildHeader('GET');
}
