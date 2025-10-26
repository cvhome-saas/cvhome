export function handleResponse<T>(it: Response): T | undefined {
    if (it.ok) {
        return it.json() as unknown as T;
    } else {
        return undefined;
    }
}

function buildHeader<T>(method: string, it: T) {
    return {
        method: method,
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(it)
    }
}

export function post<T>(it: T) {
    return buildHeader('POST', it);
}

export function put<T>(it: T) {
    return buildHeader('PUT', it);
}
