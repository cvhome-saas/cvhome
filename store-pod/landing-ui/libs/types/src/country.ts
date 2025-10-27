export interface Country {
    id: number;
    code: string;
    supported: boolean;
    name: string;
    zones: any[];
}

export type ReadableCountryList = Country[];
