import {Injectable} from "@angular/core";
import {SelectedStoreService} from "./selected-store.service";
import {Store} from "../models/commons";

@Injectable({
  providedIn: 'root'
})
export class ServiceUrlBuilderService {

  constructor(private selectedStoreService:SelectedStoreService) {
  }


  buildUrl(serviceName: string, path: string, params?: Record<string, string>): string {
    let url = `${serviceName}/${path}`;
    const store: Store | undefined = this.selectedStoreService.currentSelectedStore();

    let hasQuery = false;

    if (store) {
      url += `?store=${store.id.id}&pod=${store.podId.id}`;
      hasQuery = true;
    }

    if (params && Object.keys(params).length > 0) {
      const queryString = Object.entries(params)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
        .join('&');
      url += hasQuery ? `&${queryString}` : `?${queryString}`;
    }

    return url;
  }

}
