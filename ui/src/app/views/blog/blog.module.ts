import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {BlogComponent} from "./views/blog/blog.component";
import {BlogRoutingModule} from "./blog.routing.module";
import {ComponentsSharedModule} from "../../shared/components/components-shared.module";

@NgModule({
    imports: [
        ComponentsSharedModule, HttpClientModule, BlogRoutingModule
    ],
    declarations: [BlogComponent]
})
export class BlogModule {
}
