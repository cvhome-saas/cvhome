import {Injectable} from '@angular/core';
import moment from 'moment';

@Injectable({providedIn: 'root'})
export class OrderDetailsMapper {
  mapUpdateOrderPayload(info: any, billing: any, shipping: any) {
    return {
      "emailAddress": info.emailAddress,
      "billing": {
        "postalCode": billing.postalCode,
        "firstName": billing.firstName,
        "lastName": billing.lastName,
        "company": "",
        "phone": billing.phone,
        "address": billing.address,
        "city": billing.city,
        "billingAddress": false,
        "zone": billing.zone,
        "country": billing.country
      },
      "delivery": {
        "postalCode": shipping.postalCode,
        "firstName": shipping.firstName,
        "lastName": shipping.lastName,
        "company": "",
        "phone": shipping.phone,
        "address": shipping.address,
        "city": shipping.city,
        "billingAddress": false,
        "zone": shipping.zone,
        "country": shipping.country
      }
    };
  }

  mapHistoryPayload(comments: string, status: string) {
    return {
      comments: comments,
      date: moment().format('yyyy-MM-DD'),
      orderStatus: status
    };
  }
}
