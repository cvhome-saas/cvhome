'use client'
import * as React from 'react'
import {useEffect, useState} from 'react'
import {LayoutParams} from "@/types/params";
import {Link, usePathname, useRouter} from "@/i18n/navigation";
import {Cart} from "@/types/cart";
import Image from 'next/image';
import {useTranslations} from "next-intl";
import {
    NavigationMenu,
    NavigationMenuContent,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
    NavigationMenuTrigger,
    navigationMenuTriggerStyle
} from "@/components/ui/navigation-menu";
import {Button} from "@/components/ui/button";
import {Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle, SheetTrigger} from "@/components/ui/sheet";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "@/components/ui/accordion";
import {cn} from "@/lib/utils";
import {Globe, Menu, ShoppingBag, User, X} from "lucide-react";
import {Box} from "@/types/content";
import {Store} from "@/types/store";
import {parseDescription} from "@/services/description-view-util";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu";
import {StoreContext} from "@/types/store-context";
import {CartProductList} from "@/shared/Cart/CartProductList";
import {isRtl} from "@/services/direction-utils";
import {useCart} from "@store-front/hooks/use-cart";
import {useUser} from "@store-front/hooks/use-user";

export const Header = ({params, headerBox}: {
    params: LayoutParams,
    headerBox: Box | undefined,
}) => {
    const t = useTranslations('COMPONENTS.HEADER');
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [cartOpen, setCartOpen] = useState(false);
    const storeContext = params.storeContext;
    const {cart} = useCart(storeContext);
    const {user, login, logout} = useUser(storeContext);
    const pathname = usePathname();

    useEffect(() => {
        setCartOpen(false);
        setMobileMenuOpen(false);
    }, [pathname]);

    return (
        <>
            {/* Announcement bar */}
            <HeaderTop store={params.store} box={headerBox} locale={params.locale}/>

            {/* Store name / brand line */}
            <div className="bg-background border-b border-border py-5 text-center">
                <Link prefetch={false} href="/" className="inline-block group">
                    {params.store.logo ? (
                        <Image
                            alt={params.store.logo.name}
                            src={params.store.logo.path}
                            width={48}
                            height={48}
                            className="mx-auto h-12 w-auto"
                        />
                    ) : (
                        <span
                            className="font-['Cormorant_Garamond',serif] text-3xl font-light tracking-[0.25em] uppercase text-foreground group-hover:text-primary transition-colors duration-300">
                            {params.store.name}
                        </span>
                    )}
                </Link>
            </div>

            {/* Navigation bar */}
            <header className="bg-background border-b border-border sticky top-0 z-40 shadow-sm">
                <nav
                    aria-label="Global"
                    className="mx-auto flex max-w-7xl items-center justify-between px-6 py-3 lg:px-8"
                >
                    {/* Mobile hamburger */}
                    <div className="flex lg:hidden">
                        <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
                            <SheetTrigger asChild>
                                <Button variant="ghost" size="icon" aria-label={t('OPEN_MENU')}>
                                    <Menu className="size-5"/>
                                </Button>
                            </SheetTrigger>
                            <SheetContent side="left" className="w-full sm:max-w-sm bg-background">
                                <MobileNavContent params={params} cart={cart} setCartOpen={setCartOpen}/>
                            </SheetContent>
                        </Sheet>
                    </div>

                    {/* Desktop nav links */}
                    <div className="flex-1 flex justify-center">
                        <NavigationMenu className="hidden lg:flex">
                        <NavigationMenuList className="flex-wrap">
                            <NavigationMenuItem>
                                <Link href="/" legacyBehavior passHref>
                                    <NavigationMenuLink
                                        className={cn(navigationMenuTriggerStyle(), "font-['Jost',sans-serif] text-xs tracking-widest uppercase font-medium")}>
                                        {t('HOME_TITLE')}
                                    </NavigationMenuLink>
                                </Link>
                            </NavigationMenuItem>

                            {params.categories?.content?.filter(it => it.description).map((category) => (
                                <NavigationMenuItem key={category.code}>
                                    {category.children && category.children.length > 0 ? (
                                        <>
                                            <Link href={`/category/${category.description.friendlyUrl}`}
                                                  legacyBehavior passHref>
                                                <NavigationMenuTrigger
                                                    className="font-['Jost',sans-serif] text-xs tracking-widest uppercase font-medium cursor-pointer">
                                                    {category.description.name}
                                                </NavigationMenuTrigger>
                                            </Link>
                                            <NavigationMenuContent>
                                                <ul className="grid w-[360px] gap-1 p-3 md:w-[440px] md:grid-cols-2 lg:w-[540px]">
                                                    {category.children.filter(child => child.description).map((child) => (
                                                        <ListItem
                                                            key={child.code}
                                                            href={`/category/${child.description.friendlyUrl}`}
                                                            title={child.description.name}
                                                        />
                                                    ))}
                                                </ul>
                                            </NavigationMenuContent>
                                        </>
                                    ) : (
                                        <Link href={`/category/${category.description.friendlyUrl}`}
                                              legacyBehavior passHref>
                                            <NavigationMenuLink
                                                className={cn(navigationMenuTriggerStyle(), "font-['Jost',sans-serif] text-xs tracking-widest uppercase font-medium")}>
                                                {category.description.name}
                                            </NavigationMenuLink>
                                        </Link>
                                    )}
                                </NavigationMenuItem>
                            ))}

                            {params.contents?.content?.filter(it => it.linkToMenu && it.visible && it.description).map(it => (
                                <NavigationMenuItem key={it.code}>
                                    <Link href={`/content/${it.description.friendlyUrl}`} legacyBehavior passHref>
                                        <NavigationMenuLink
                                            className={cn(navigationMenuTriggerStyle(), "font-['Jost',sans-serif] text-xs tracking-widest uppercase font-medium")}>
                                            {it.description.name}
                                        </NavigationMenuLink>
                                    </Link>
                                </NavigationMenuItem>
                            ))}
                        </NavigationMenuList>
                    </NavigationMenu>
                    </div>

                    {/* Right side: user + cart */}
                    <div className="flex items-center gap-1">
                        {user ? (
                            <div className="flex items-center gap-2">
                                <span className="text-xs tracking-wider text-muted-foreground hidden sm:inline uppercase">
                                    {user?.claims?.username}
                                </span>
                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <Button variant="ghost" size="icon" className="rounded-none" aria-label="User menu">
                                            <User className="size-5"/>
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent align="end" className="rounded-none">
                                        <DropdownMenuItem
                                            onClick={logout}
                                            className="cursor-pointer text-xs tracking-widest uppercase text-destructive focus:text-destructive">
                                            {t('LOGOUT')}
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            </div>
                        ) : (
                            <Button
                                variant="ghost"
                                onClick={login}
                                className="text-xs tracking-widest uppercase font-medium text-foreground hover:text-primary rounded-none"
                            >
                                {t('LOGIN')}
                            </Button>
                        )}

                        <Button
                            variant="ghost"
                            size="icon"
                            className="relative rounded-none"
                            onClick={() => setCartOpen(true)}
                            aria-label={t('OPEN_CART')}
                        >
                            <ShoppingBag className="size-5 text-foreground"/>
                            {cart && cart.quantity > 0 && (
                                <span
                                    className="absolute -top-0.5 -end-0.5 flex items-center justify-center h-4 w-4 rounded-full bg-primary text-[10px] font-medium text-primary-foreground">
                                    {cart.quantity}
                                </span>
                            )}
                        </Button>
                    </div>
                </nav>

                <NavCartDialog
                    storeContext={params.storeContext}
                    cart={cart}
                    cartOpen={cartOpen}
                    setCartOpen={setCartOpen}
                />
            </header>
        </>
    );
};

const MobileNavContent = ({params, cart, setCartOpen}: {
    params: LayoutParams,
    cart: Cart | undefined,
    setCartOpen: (open: boolean) => void
}) => {
    const t = useTranslations('COMPONENTS.HEADER');
    return (
        <div className="mt-6 flow-root">
            <div className="-my-6 divide-y divide-border">
                <div className="space-y-1 py-6">
                    <Link
                        href="/"
                        className="block px-3 py-2 text-xs tracking-widest uppercase font-medium text-foreground hover:text-primary hover:bg-accent rounded-sm transition-colors">
                        {t('HOME_TITLE')}
                    </Link>

                    <Accordion type="multiple" className="w-full">
                        {params.categories?.content?.filter(it => it.description).map((category) => (
                            category.children && category.children.length > 0 ? (
                                <AccordionItem value={category.code} key={category.code} className="border-b-0">
                                    <div className="flex items-center justify-between hover:bg-accent rounded-sm">
                                        <Link
                                            href={`/category/${category.description.friendlyUrl}`}
                                            className="grow px-3 py-2 text-xs tracking-widest uppercase font-medium text-foreground">
                                            {category.description.name}
                                        </Link>
                                        <AccordionTrigger className="p-2 me-1.5"/>
                                    </div>
                                    <AccordionContent className="ps-6">
                                        {category.children.filter(child => child.description).map((child) => (
                                            <Link
                                                key={child.code}
                                                href={`/category/${child.description.friendlyUrl}`}
                                                className="block py-2 text-xs tracking-wider text-muted-foreground hover:text-primary hover:bg-accent rounded-sm px-3 transition-colors">
                                                {child.description.name}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            ) : (
                                <Link
                                    key={category.code}
                                    href={`/category/${category.description.friendlyUrl}`}
                                    className="block px-3 py-2 text-xs tracking-widest uppercase font-medium text-foreground hover:text-primary hover:bg-accent rounded-sm transition-colors">
                                    {category.description.name}
                                </Link>
                            )
                        ))}
                    </Accordion>

                    {params.contents?.content?.filter(it => it.linkToMenu && it.visible && it.description).map(it => (
                        <Link
                            key={it.code}
                            href={`/content/${it.description.friendlyUrl}`}
                            className="block px-3 py-2 text-xs tracking-widest uppercase font-medium text-foreground hover:text-primary hover:bg-accent rounded-sm transition-colors">
                            {it.description.name}
                        </Link>
                    ))}
                </div>

                <div className="py-6">
                    <Button
                        variant="ghost"
                        size="icon"
                        className="relative rounded-none"
                        onClick={() => setCartOpen(true)}
                        aria-label={t('OPEN_CART')}
                    >
                        <ShoppingBag className="size-5 text-foreground"/>
                        {cart && cart.quantity > 0 && (
                            <span
                                className="absolute -top-0.5 -end-0.5 flex items-center justify-center h-4 w-4 rounded-full bg-primary text-[10px] font-medium text-primary-foreground">
                                {cart.quantity}
                            </span>
                        )}
                    </Button>
                </div>
            </div>
        </div>
    );
};

const ListItem = React.forwardRef<HTMLAnchorElement, React.ComponentPropsWithoutRef<typeof Link>>(
    ({className, title, children, href, ...props}, ref) => {
        return (
            <li>
                <NavigationMenuLink asChild>
                    <Link
                        ref={ref}
                        href={href}
                        className={cn(
                            "block select-none p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
                            className
                        )}
                        {...props}
                    >
                        <div className="text-xs font-medium tracking-widest uppercase leading-none">{title}</div>
                        {children && (
                            <p className="line-clamp-2 text-xs leading-snug text-muted-foreground mt-1">
                                {children}
                            </p>
                        )}
                    </Link>
                </NavigationMenuLink>
            </li>
        );
    }
);
ListItem.displayName = "ListItem";

interface HeaderTopProps extends React.HTMLAttributes<HTMLDivElement> {
    store: Store;
    box: Box | undefined;
    locale: string;
}

const HeaderTop = React.forwardRef<HTMLDivElement, HeaderTopProps>(
    ({store, box, locale, className, ...props}, ref) => {
        return (
            <div
                ref={ref}
                className={cn("relative flex items-center justify-center bg-foreground min-h-9", className)}
                {...props}
            >
                {box?.visible && (
                    <div
                        className="grow h-9 flex items-center justify-center px-4 text-xs font-light tracking-widest uppercase text-primary text-center sm:px-6 lg:px-8"
                        dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}
                    />
                )}
                <div className="absolute end-4">
                    <LanguageSelector store={store} locale={locale}/>
                </div>
            </div>
        );
    }
);
HeaderTop.displayName = "HeaderTop";

export const LanguageSelector = ({store, locale}: { store: Store, locale: string }) => {
    const t = useTranslations('COMPONENTS.HEADER.LANGUAGE');
    const router = useRouter();
    const pathname = usePathname();

    if (!store.supportedLanguages || store.supportedLanguages.length <= 1) {
        return null;
    }

    const currentLocale = store.supportedLanguages.includes(locale) ? locale : 'en';

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="text-primary hover:text-primary/80 hover:bg-transparent" aria-label={t('CHANGE_LANGUAGE')}>
                    <Globe className="size-4"/>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="rounded-none">
                {store.supportedLanguages.map((lang) => (
                    <DropdownMenuItem
                        key={lang}
                        onClick={() => router.push(pathname, {locale: lang})}
                        className="cursor-pointer text-xs tracking-widest uppercase"
                        disabled={lang === currentLocale}
                    >
                        {t(lang.toUpperCase())}
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
};

export const NavCartDialog = (
    {storeContext, cart, cartOpen, setCartOpen}: {
        storeContext: StoreContext,
        cart: Cart | undefined,
        cartOpen: boolean,
        setCartOpen: (cart: boolean) => void
    }
) => {
    const t = useTranslations("COMPONENTS.CART");
    const isRtlLayout = isRtl(storeContext.locale);

    return (
        <Sheet open={cartOpen} onOpenChange={setCartOpen}>
            <SheetContent
                side={isRtlLayout ? "left" : "right"}
                className="flex h-full flex-col p-0 bg-background max-w-md w-full rounded-none"
            >
                <div className="flex-1 overflow-y-auto px-6 py-6">
                    <SheetHeader className="flex flex-row items-start justify-between space-y-0 mb-6">
                        <SheetTitle className="font-['Cormorant_Garamond',serif] text-xl font-light tracking-widest uppercase text-foreground">
                            {t('CART_DETAILS')}
                        </SheetTitle>
                        <SheetClose
                            type="button"
                            className="relative -m-2 p-2 text-muted-foreground hover:text-foreground transition-colors"
                            aria-label="Close cart"
                        >
                            <X aria-hidden="true" className="size-5"/>
                        </SheetClose>
                    </SheetHeader>

                    <CartProductList storeContext={storeContext} cart={cart}/>
                </div>

                <div className="border-t border-border px-6 py-6">
                    {cart && cart.products && cart.products.length > 0 && (
                        <>
                            <div className="flex justify-between text-sm font-medium text-foreground mb-1">
                                <p className="uppercase tracking-widest text-xs">{t('SUB_TOTAL')}</p>
                                <p className="font-['Cormorant_Garamond',serif] text-base">{cart.displaySubTotal}</p>
                            </div>
                            <p className="text-xs text-muted-foreground tracking-wide mb-5">
                                {t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}
                            </p>
                            <Link
                                prefetch={false}
                                href="/checkout"
                                className="flex items-center justify-center w-full border border-foreground bg-foreground text-background px-6 py-3 text-xs font-medium tracking-widest uppercase hover:bg-primary hover:border-primary hover:text-primary-foreground transition-colors duration-300"
                            >
                                {t('CHECKOUT')}
                            </Link>
                        </>
                    )}
                    <div className="mt-4 flex justify-center text-center text-xs text-muted-foreground">
                        <p>
                            {t('OR')}{' '}
                            <SheetClose type="button" className="font-medium text-foreground hover:text-primary underline underline-offset-2">
                                {t('CONTINUE_SHOPPING')}
                            </SheetClose>
                        </p>
                    </div>
                </div>
            </SheetContent>
        </Sheet>
    );
};
