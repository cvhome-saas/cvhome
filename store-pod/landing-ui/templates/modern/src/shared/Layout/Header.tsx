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
import {Globe, Menu, ShoppingBag, X} from "lucide-react";
import {Box} from "@/types/content";
import {Store} from "@/types/store";
import {parseDescription} from "@/services/description-view-util";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu";
import {StoreContext} from "@/types/store-context";
import {CartProductList} from "@/shared/Cart/CartProductList";
import {isRtl} from "@/services/direction-utils";
import {useCart} from "@store-front/hooks/use-cart";
import {AuthService} from "@store-front/services/auth-service";
import {User} from "lucide-react";

export const Header = ({params, headerBox}: {
    params: LayoutParams,
    headerBox: Box | undefined,
}) => {
    const t = useTranslations('COMPONENTS.HEADER');
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
    const [cartOpen, setCartOpen] = useState(false)
    const storeContext = params.storeContext;
    const {cart} = useCart(storeContext);
    const pathname = usePathname();
    const [user, setUser] = useState<any>(null);

    useEffect(() => {
        AuthService.getMe(storeContext).then(setUser).catch(() => setUser(null));
    }, [storeContext]);

    const handleLogin = async () => {
        await AuthService.login(storeContext);
    };

    useEffect(() => {
        setCartOpen(false);
        setMobileMenuOpen(false);
    }, [pathname]);

    return (
        <>
            <HeaderTop store={params.store} box={headerBox} locale={params.locale}/>
            <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/70">
                <nav aria-label="Global" className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
                    <div className="flex items-center gap-3">
                        <Link prefetch={false} href={"/"} className="group inline-flex items-center gap-3">
                            <span className="sr-only">{params.store.name}</span>
                            {params.store.logo && (
                                <Image
                                    alt={params.store.logo.name}
                                    src={params.store.logo.path}
                                    width={32}
                                    height={32}
                                    className="h-9 w-9 rounded-xl object-contain transition-transform duration-300 ease-out group-hover:scale-[1.03]"
                                />
                            )}
                        </Link>
                    </div>
                    <div className="flex-1 flex justify-center">
                        <NavigationMenu className="hidden lg:flex">
                            <NavigationMenuList className="flex-wrap">
                                <NavigationMenuItem>
                                    <Link href="/" legacyBehavior passHref>
                                        <NavigationMenuLink className={cn(navigationMenuTriggerStyle(), "tracking-[0.12em] uppercase text-xs")}>
                                            {t('HOME_TITLE')}
                                        </NavigationMenuLink>
                                    </Link>
                                </NavigationMenuItem>

                                {params.categories?.content?.filter(it => it.description).map((category) => (
                                    <NavigationMenuItem key={category.code}>
                                        {category.children && category.children.length > 0 ? (
                                            <>
                                                <Link href={`/category/${category.description.friendlyUrl}`} legacyBehavior passHref>
                                                    <NavigationMenuTrigger className="cursor-pointer tracking-[0.12em] uppercase text-xs">
                                                        {category.description.name}
                                                    </NavigationMenuTrigger>
                                                </Link>
                                                <NavigationMenuContent>
                                                    <ul className="grid w-[420px] gap-3 p-4 md:w-[520px] md:grid-cols-2 lg:w-[640px]">
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
                                            <Link href={`/category/${category.description.friendlyUrl}`} legacyBehavior passHref>
                                                <NavigationMenuLink className={cn(navigationMenuTriggerStyle(), "tracking-[0.12em] uppercase text-xs")}>
                                                    {category.description.name}
                                                </NavigationMenuLink>
                                            </Link>
                                        )}
                                    </NavigationMenuItem>
                                ))}

                                {params.contents?.content?.filter(it => it.linkToMenu && it.visible && it.description).map(it => (
                                    <NavigationMenuItem key={it.code}>
                                        <Link href={`/content/${it.description.friendlyUrl}`} legacyBehavior passHref>
                                            <NavigationMenuLink className={cn(navigationMenuTriggerStyle(), "tracking-[0.12em] uppercase text-xs")}>
                                                {it.description.name}
                                            </NavigationMenuLink>
                                        </Link>
                                    </NavigationMenuItem>
                                ))}
                            </NavigationMenuList>
                        </NavigationMenu>
                    </div>

                    <div className="flex items-center justify-end gap-2">
                        <div className="hidden lg:flex">
                            <LanguageSelector store={params.store} locale={params.locale}/>
                        </div>

                        {user ? (
                            <div className="flex items-center gap-2">
                                <span className="text-xs font-medium text-muted-foreground hidden sm:inline">
                                    {user.firstName || user.username}
                                </span>
                                <Button variant="ghost" size="icon" className="rounded-full">
                                    <User className="size-5" />
                                </Button>
                            </div>
                        ) : (
                            <Button variant="ghost" size="sm" onClick={handleLogin} className="tracking-[0.12em] uppercase text-xs">
                                {t('LOGIN') || 'Login'}
                            </Button>
                        )}

                        <div className="flex lg:hidden">
                            <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
                                <SheetTrigger asChild>
                                    <Button variant="outline" size="icon" className="rounded-full">
                                        <Menu className="size-5"/>
                                        <span className="sr-only">{t('OPEN_MENU')}</span>
                                    </Button>
                                </SheetTrigger>
                                <SheetContent side="left" className="w-full sm:max-w-sm">
                                    <MobileNavContent params={params} cart={cart} setCartOpen={setCartOpen}/>
                                </SheetContent>
                            </Sheet>
                        </div>

                        <Button
                            variant="outline"
                            size="icon"
                            className="relative rounded-full"
                            onClick={() => setCartOpen(true)}
                        >
                            <ShoppingBag aria-hidden="true" className="size-5 text-foreground"/>
                            {cart && cart.quantity > 0 && (
                                <span
                                    className="absolute -top-1 -end-1 grid h-5 w-5 place-items-center rounded-full bg-primary text-[11px] font-semibold text-primary-foreground">
                                    {cart.quantity}
                                </span>
                            )}
                            <span className="sr-only">{t('OPEN_CART')}</span>
                        </Button>
                    </div>
                </nav>

                <NavCartDialog storeContext={params.storeContext} cart={cart} cartOpen={cartOpen} setCartOpen={setCartOpen}/>
            </header>
        </>
    )
}



const MobileNavContent = ({params, cart, setCartOpen}: {
    params: LayoutParams,
    cart: Cart | undefined,
    setCartOpen: (open: boolean) => void
}) => {
    const t = useTranslations('COMPONENTS.HEADER');
    return (
        <div className="mt-6 flow-root">
            <div className="-my-6 divide-y divide-border">
                <div className="space-y-3 py-6">
                    <Link
                        href="/"
                        className="-mx-3 block rounded-xl px-3 py-3 text-sm font-semibold tracking-[0.12em] uppercase text-foreground hover:bg-accent"
                    >
                        {t('HOME_TITLE')}
                    </Link>

                    <Accordion type="multiple" className="w-full">
                        {params.categories?.content?.filter(it => it.description).map((category) => (
                            category.children && category.children.length > 0 ? (
                                <AccordionItem value={category.code} key={category.code} className="border-b-0">
                                    <div className="flex items-center justify-between rounded-xl hover:bg-accent">
                                        <Link
                                            href={`/category/${category.description.friendlyUrl}`}
                                            className="grow px-3 py-3 text-sm font-semibold tracking-[0.12em] uppercase text-foreground"
                                        >
                                            {category.description.name}
                                        </Link>
                                        <AccordionTrigger className="p-2 me-2"/>
                                    </div>
                                    <AccordionContent className="ps-6">
                                        {category.children.filter(child => child.description).map((child) => (
                                            <Link
                                                key={child.code}
                                                href={`/category/${child.description.friendlyUrl}`}
                                                className="block rounded-xl py-2 text-sm text-foreground hover:bg-accent"
                                            >
                                                {child.description.name}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            ) : (
                                <Link
                                    key={category.code}
                                    href={`/category/${category.description.friendlyUrl}`}
                                    className="-mx-3 block rounded-xl px-3 py-3 text-sm font-semibold tracking-[0.12em] uppercase text-foreground hover:bg-accent"
                                >
                                    {category.description.name}
                                </Link>
                            )
                        ))}
                    </Accordion>

                    {params.contents?.content?.filter(it => it.linkToMenu && it.visible && it.description).map(it => (
                        <Link
                            key={it.code}
                            href={`/content/${it.description.friendlyUrl}`}
                            className="-mx-3 block rounded-xl px-3 py-3 text-sm font-semibold tracking-[0.12em] uppercase text-foreground hover:bg-accent"
                        >
                            {it.description.name}
                        </Link>
                    ))}
                </div>

                <div className="py-6">
                    <LanguageSelector store={params.store} locale={params.locale} className="w-full"/>
                </div>
                <div className="py-6">
                    <Button variant="outline" className="relative w-full rounded-xl justify-center" onClick={() => setCartOpen(true)}>
                        <ShoppingBag aria-hidden="true" className="me-2 size-5 text-foreground"/>
                        <span className="text-sm font-semibold tracking-[0.12em] uppercase">{t('OPEN_CART')}</span>
                        {cart && cart.quantity > 0 && (
                            <span className="ms-2 rounded-full bg-primary px-2 py-0.5 text-[11px] font-semibold text-primary-foreground">
                                {cart.quantity}
                            </span>
                        )}
                    </Button>
                </div>
            </div>
        </div>
    );
}

const ListItem = React.forwardRef<HTMLAnchorElement, React.ComponentPropsWithoutRef<"a">>(
    ({className, title, children, ...props}, ref) => {
        return (
            <li>
                <NavigationMenuLink asChild>
                    <a
                        ref={ref}
                        className={cn(
                            "block select-none rounded-xl bg-transparent p-4 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
                            className
                        )}
                        {...props}
                    >
                        <div className="text-sm font-medium leading-none">{title}</div>
                        <p className="mt-1 line-clamp-2 text-sm leading-snug text-muted-foreground">
                            {children}
                        </p>
                    </a>
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
                className={cn("relative flex items-center justify-center", className)}
                {...props}
            >
                {box?.visible && (
                    <div
                        className="grow border-b border-border bg-secondary/40 px-4 py-2 text-center text-xs text-foreground backdrop-blur sm:px-6 lg:px-8"
                        dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}
                    />
                )}
            </div>
        );
    }
);
HeaderTop.displayName = "HeaderTop";


export const LanguageSelector = ({store, locale, className}: {
    store: Store,
    locale: string,
    className?: string
}) => {
    const t = useTranslations('COMPONENTS.HEADER.LANGUAGE');
    const router = useRouter();
    const pathname = usePathname();

    if (!store.supportedLanguages || store.supportedLanguages.length <= 1) {
        return null;
    }

    const currentLocale = store.supportedLanguages.includes(locale) ? locale : 'en';

    return (
        <DropdownMenu dir={isRtl(locale) ? 'rtl' : 'ltr'}>
            <DropdownMenuTrigger asChild>
                <Button variant="outline" size="icon" className="rounded-full">
                    <Globe className="size-5"/>
                    <span className="sr-only">{t('CHANGE_LANGUAGE')}</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                {store.supportedLanguages.sort().map((lang) => (
                    <DropdownMenuItem
                        key={lang}
                        onClick={() => router.push(pathname, {locale: lang})}
                        className="cursor-pointer"
                        disabled={lang === currentLocale}
                    >
                        {t(lang.toUpperCase())}
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

export const NavCartDialog = (
    {
        storeContext,
        cart,
        cartOpen,
        setCartOpen
    }: {
        storeContext: StoreContext,
        cart: Cart | undefined,
        cartOpen: boolean,
        setCartOpen: (cart: boolean) => void
    },
) => {
    const t = useTranslations("COMPONENTS.CART");
    const isRtlLayout = isRtl(storeContext.locale);

    return (
        <Sheet open={cartOpen} onOpenChange={setCartOpen}>
            <SheetContent
                side={isRtlLayout ? "left" : "right"}
                className="flex h-full flex-col p-0 bg-background shadow-xl max-w-md w-full"
            >
                <div className="flex-1 overflow-y-auto px-4 py-6 sm:px-6">
                    <SheetHeader className="flex flex-row items-start justify-between space-y-0">
                        <SheetTitle className="text-sm font-semibold tracking-[0.18em] uppercase text-foreground">
                            {t('CART_DETAILS')}
                        </SheetTitle>

                        <div className={`${isRtlLayout ? 'me-3' : 'ms-3'} flex h-7 items-center`}>
                            <SheetClose
                                type="button"
                                className="rounded-full border border-border bg-background p-2 text-foreground hover:bg-accent"
                            >
                                <X aria-hidden="true" className="size-5"/>
                            </SheetClose>
                        </div>
                    </SheetHeader>

                    <div className="mt-6">
                        <CartProductList storeContext={storeContext} cart={cart}/>
                    </div>
                </div>

                <div className="border-t border-border px-4 py-6 sm:px-6">
                    {cart && cart.products && cart.products.length > 0 && (
                        <>
                            <div className="space-y-2 pt-2">
                                <div className="flex justify-between text-sm font-medium text-foreground">
                                    <p>{t('SUB_TOTAL')}</p>
                                    <p>{cart.displaySubTotal}</p>
                                </div>
                            </div>

                            <p className="mt-2 text-xs text-muted-foreground">
                                {t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}
                            </p>

                            <div className="mt-5">
                                <Link
                                    prefetch={false}
                                    href="/checkout"
                                    className="flex items-center justify-center rounded-xl bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground"
                                >
                                    {t('CHECKOUT')}
                                </Link>
                            </div>
                        </>
                    )}

                    <div className="mt-4 flex justify-center text-center text-xs text-muted-foreground">
                        <p>
                            {t('OR')}{' '}
                            <SheetClose type="button" className="font-semibold text-foreground hover:underline">
                                {t('CONTINUE_SHOPPING')}
                            </SheetClose>
                        </p>
                    </div>
                </div>
            </SheetContent>
        </Sheet>
    );
}