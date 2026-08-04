import {Injectable} from '@angular/core';
import moment from 'moment';
import {CustomerAddress, PersistableOrderStatusHistory, UpdateOrderPayload} from '../../models/order.model';

interface OrderInfo {
  emailAddress?: string;
}

@Injectable({providedIn: 'root'})
export class OrderDetailsMapper {
  mapUpdateOrderPayload(info: OrderInfo, billing: CustomerAddress, shipping: CustomerAddress): UpdateOrderPayload {
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

  mapHistoryPayload(comments: string, status: string): PersistableOrderStatusHistory {
    return {
      comments: comments,
      date: moment().format('yyyy-MM-DD'),
      orderStatus: status
    };
  }
}
