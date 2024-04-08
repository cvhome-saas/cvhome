import {Injectable} from '@angular/core';
import {CategoryList} from "../domain/category";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private readonly CATEGORY_BASE_URL: string = '/store/api/v1/category';

  constructor(private httpClient: HttpClient) {
  }


  list(): Observable<CategoryList> {
    return this.httpClient.get<CategoryList>(`${this.CATEGORY_BASE_URL}`)
  }
}
