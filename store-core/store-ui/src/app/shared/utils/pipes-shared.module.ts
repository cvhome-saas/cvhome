import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MapErrorsPipe} from "./pipe/map-errors.pipe";

@NgModule({
    declarations: [
        MapErrorsPipe,
    ],
    imports: [
        CommonModule
    ],
    exports: [
        MapErrorsPipe
    ]
})
export class PipesSharedModule {
}
