import {Pipe, PipeTransform} from '@angular/core';
import {ValidationErrors} from "@angular/forms";

@Pipe({
  name: 'mapErrors'
})
export class MapErrorsPipe implements PipeTransform {

  transform(value: ValidationErrors | null, ...args: unknown[]): any[] {
    if (value) {
      return Object.keys(value);

    } else {
      return [];
    }
  }

}
