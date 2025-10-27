import {Store} from "./store";
import {Cart} from "./cart";
import {CategoryPage} from "./category";
import {ContentPage} from "./content";
import {StoreContext} from "./store-context";
import {Product} from "./product-groups";

export interface LocaleParam {
    locale: string;
}

export interface StoreContextParam {
    storeContext: StoreContext;
}

export interface StoreParams extends LocaleParam {
    store: Store;
}

export interface ProductParams extends LocaleParam {
    product: Product;
}

export interface CategoriesParams extends LocaleParam {
    categories: CategoryPage | undefined;
}

export interface ContentsParams extends LocaleParam {
    contents: ContentPage | undefined;
}

export interface CartParams extends LocaleParam {
    cart: Cart;
}

export type Params =
    LocaleParam
    | StoreParams
    | StoreContextParam
    | ProductPageParams
    | CategoriesParams
    | ContentsParams
    | CartPageParams;

export interface DefaultParams extends LocaleParam, StoreParams, StoreContextParam {
}

export interface LayoutParams extends DefaultParams, CategoriesParams, ContentsParams, CartParams {
}

export interface DefaultPageParams {
    locale: string;
    storeContext: StoreContext;
}

export interface CartPageParams extends DefaultPageParams {
}

export interface CategoryPageParams extends DefaultPageParams {
    url: string;
}

export interface ProductPageParams extends DefaultPageParams {
    url: string;
}

export interface ContactPageParams extends DefaultPageParams {
}

export interface CheckoutPageParams extends DefaultPageParams {
}

export interface ContentPageParams extends DefaultPageParams {
    url: string;
}

export interface HomePageParams extends DefaultPageParams {
}
