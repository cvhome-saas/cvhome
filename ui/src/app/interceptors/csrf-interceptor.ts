import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable()
export class CsrfInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let csrfToken = this.getCsrfToken();
    if (csrfToken) {
      const newReq = req.clone({
        headers: req.headers
          // .set("X-CSRF-TOKEN", csrfToken).set("CSRF-TOKEN", csrfToken).set("XSRF-TOKEN", csrfToken).set("X-XSRF-TOKEN", csrfToken)
      });
      return next.handle(newReq);
    } else {
      return next.handle(req);

    }

  }


  getCsrfToken(): string | null {
    let allCookies = document.cookie;
    let csrfEntry = allCookies.split(";").find(it => it.startsWith("XSRF-TOKEN"));
    if (csrfEntry) {
      return csrfEntry.replace("XSRF-TOKEN=", "")
    } else {
      return null;
    }


  }
}
