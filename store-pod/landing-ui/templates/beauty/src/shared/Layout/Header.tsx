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
import {useUser} from "@store-front/hooks/use-user";
import {User} from "lucide-react";

export const Header = ({params, headerBox}: {
    params: LayoutParams,
    headerBox: Box | undefined,}) => {
    const t = useTranslations('COMPONENTS.HEADER');
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
    const [cartOpen, setCartOpen] = useState(false)
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
            <HeaderTop store={params.store} box={headerBox} locale={params.locale}/>
            <header className="bg-background/80 backdrop-blur-md sticky top-0 z-50 border-b border-primary/20 shadow-sm">
                <nav aria-label="Global" className="mx-auto flex max-w-7xl items-center justify-between p-4 lg:px-8">
                    <div className="flex items-center">
                        <Link prefetch={false} href={"/"} className="-m-1.5 p-1.5 group flex items-center gap-2">
                            <span className="sr-only">{params.store.name}</span>
                            {
                                params.store.logo &&
                                <Image
                                    alt={params.store.logo.name}
                                    src={params.store.logo.path}
                                    width={40}
                                    height={40}
                                    className="h-10 w-auto transition-all duration-500 ease-in-out group-hover:rotate-12 group-hover:scale-110"
                                />
                            }
                            <span className="text-xl font-serif font-bold tracking-tight text-primary">{params.store.name}</span>
                        </Link>
                    </div>
                    <div className="flex lg:hidden">
                        <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
                            <SheetTrigger asChild>
                                <Button variant="ghost" size="icon" className="lg:hidden">
                                    <Menu className="size-6"/>
                                    <span className="sr-only">{t('OPEN_MENU')}</span>
                                </Button>
                            </SheetTrigger>
                            <SheetContent side="left" className="w-full sm:max-w-sm">
                                <MobileNavContent params={params} cart={cart} setCartOpen={setCartOpen}/>
                            </SheetContent>
                        </Sheet>
                    </div>
                    <div className="flex-1 flex justify-center">
                    <NavigationMenu className="hidden lg:flex">
                        <NavigationMenuList className="flex-wrap">
                            <NavigationMenuItem>
                                <Link href="/" legacyBehavior passHref>
                                    <NavigationMenuLink className={cn(navigationMenuTriggerStyle(), "bg-transparent hover:bg-primary/5 text-foreground hover:text-primary transition-colors font-serif font-medium uppercase tracking-wider text-sm")}>
                                        {t('HOME_TITLE')}
                                    </NavigationMenuLink>
                                </Link>
                            </NavigationMenuItem>

                            {params.categories?.content?.filter(it => it.description).map((category) => (
                                <NavigationMenuItem key={category.code}>
                                    {category.children && category.children.length > 0 ? (
                                        <>
                                            <Link href={`/category/${category.description.friendlyUrl}`} legacyBehavior
                                                  passHref>
                                                <NavigationMenuTrigger className="cursor-pointer bg-transparent hover:bg-primary/5 text-foreground hover:text-primary transition-colors font-serif font-medium uppercase tracking-wider text-sm">
                                                    {category.description.name}
                                                </NavigationMenuTrigger>
                                            </Link>
                                            <NavigationMenuContent>
                                                <ul className="grid w-[400px] gap-3 p-4 md:w-[500px] md:grid-cols-2 lg:w-[600px]">
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
                                        <Link href={`/category/${category.description.friendlyUrl}`} legacyBehavior
                                              passHref>
                                            <NavigationMenuLink className={cn(navigationMenuTriggerStyle(), "bg-transparent hover:bg-primary/5 text-foreground hover:text-primary transition-colors font-serif font-medium uppercase tracking-wider text-sm")}>
                                                {category.description.name}
                                            </NavigationMenuLink>
                                        </Link>
                                    )}
                                </NavigationMenuItem>
                            ))}

                            {params.contents?.content?.filter(it => it.linkToMenu && it.visible && it.description).map(it => (
                                <NavigationMenuItem key={it.code}>
                                    <Link href={`/content/${it.description.friendlyUrl}`} legacyBehavior passHref>
                                        <NavigationMenuLink className={cn(navigationMenuTriggerStyle(), "bg-transparent hover:bg-primary/5 text-foreground hover:text-primary transition-colors font-serif font-medium uppercase tracking-wider text-sm")}>
                                            {it.description.name}
                                        </NavigationMenuLink>
                                    </Link>
                                </NavigationMenuItem>
                            ))}
                        </NavigationMenuList>
                    </NavigationMenu>
                    </div>

                    <div className="hidden lg:flex items-center gap-2">
                        {user ? (
                            <div className="flex items-center gap-2">
                                <span className="text-sm font-medium text-muted-foreground hidden sm:inline">
                                    {user?.claims?.username}
                                </span>
                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <Button variant="ghost" size="icon" className="rounded-full">
                                            <User className="size-6" />
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent align="end">
                                        <DropdownMenuItem onClick={logout} className="cursor-pointer text-destructive focus:text-destructive">
                                            {t('LOGOUT')}
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            </div>
                        ) : (
                            <Button variant="ghost" onClick={login} className="text-sm font-semibold leading-6 text-foreground">
                                {t('LOGIN')} <span aria-hidden="true">&rarr;</span>
                            </Button>
                        )}
                        <Button variant="ghost" className="relative" onClick={() => setCartOpen(true)}>
                            <ShoppingBag
                                aria-hidden="true"
                                className="size-6 text-foreground"
                            />
                            {cart && cart.quantity > 0 && (
                                <span
                                    className="absolute top-0 right-0 -mt-1 -mr-1 flex items-center justify-center h-5 w-5 rounded-full bg-primary text-xs font-medium text-primary-foreground">
                                {cart.quantity}
                            </span>
                            )}
                            <span className="sr-only">{t('OPEN_CART')}</span>
                        </Button>
                    </div>
                </nav>
                <NavCartDialog storeContext={params.storeContext} cart={cart} cartOpen={cartOpen}
                               setCartOpen={setCartOpen}/>
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
                <div className="space-y-2 py-6">
                    <Link href="/"
                          className="-mx-3 block rounded-lg px-3 py-2 text-base font-semibold leading-7 text-foreground hover:bg-accent">
                        {t('HOME_TITLE')}
                    </Link>

                    <Accordion type="multiple" className="w-full">
                        {params.categories?.content?.filter(it => it.description).map((category) => (
                            category.children && category.children.length > 0 ? (
                                <AccordionItem value={category.code} key={category.code} className="border-b-0">
                                    <div className="flex items-center justify-between rounded-lg hover:bg-accent">
                                        <Link href={`/category/${category.description.friendlyUrl}`}
                                              className="grow px-3 py-2 text-base font-semibold leading-7 text-foreground">
                                            {category.description.name}
                                        </Link>
                                        <AccordionTrigger className="p-2 me-1.5"/>
                                    </div>
                                    <AccordionContent className="pl-6">
                                        {category.children.filter(child => child.description).map((child) => (
                                            <Link key={child.code} href={`/category/${child.description.friendlyUrl}`}
                                                  className="block rounded-lg py-2 text-sm leading-7 text-foreground hover:bg-accent">
                                                {child.description.name}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            ) : (
                                <Link key={category.code} href={`/category/${category.description.friendlyUrl}`}
                                      className="-mx-3 block rounded-lg px-3 py-2 text-base font-semibold leading-7 text-foreground hover:bg-accent">
                                    {category.description.name}
                                </Link>
                            )
                        ))}
                    </Accordion>

                    {params.contents?.content?.filter(it => it.linkToMenu && it.visible && it.description).map(it => (
                        <Link key={it.code} href={`/content/${it.description.friendlyUrl}`}
                              className="-mx-3 block rounded-lg px-3 py-2 text-base font-semibold leading-7 text-foreground hover:bg-accent">
                            {it.description.name}
                        </Link>
                    ))}
                </div>
                <div className="py-6">
                    <Button variant="ghost" className="relative" onClick={() => setCartOpen(true)}>
                        <ShoppingBag aria-hidden="true" className="size-6 text-foreground"/>
                        {cart && cart.quantity > 0 && (
                            <span
                                className="absolute top-0 right-0 -mt-1 -mr-1 flex items-center justify-center h-5 w-5 rounded-full bg-primary text-xs font-medium text-primary-foreground">
                                {cart.quantity}
                            </span>
                        )}
                        <span className="sr-only">{t('OPEN_CART')}</span>
                    </Button>
                </div>
            </div>
        </div>
    );
}

const ListItem = React.forwardRef<HTMLAnchorElement, React.ComponentPropsWithoutRef<typeof Link>>(
    ({className, title, children, href, ...props}, ref) => {
        return (
            <li>
                <NavigationMenuLink asChild>
                    <Link
                        ref={ref}
                        href={href}
                        className={cn(
                            "block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
                            className
                        )}
                        {...props}
                    >
                        <div className="text-sm font-medium leading-none">{title}</div>
                        {children && (
                            <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
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
                className={cn("relative flex items-center justify-center", className)}
                {...props}
            >
                {box?.visible && (
                    <div
                        className="grow h-10 flex items-center justify-center bg-primary px-4 text-sm font-medium text-foreground text-center sm:px-6 lg:px-8"
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
                <Button variant="ghost" size="icon">
                    <Globe className="size-6"/>
                    <span className="sr-only">{t('CHANGE_LANGUAGE')}</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                {store.supportedLanguages.map((lang) => (
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
                        <SheetTitle className="text-lg font-medium text-foreground">
                            {t('CART_DETAILS')}
                        </SheetTitle>

                        <div className={`${isRtlLayout ? 'me-3' : 'ms-3'} flex h-7 items-center`}>
                            <SheetClose
                                type="button"
                                className="relative -m-2 p-2 text-neutral hover:text-hover-neutral rounded-md"
                            >
                                <span className="sr-only">Close panel</span>
                                <X aria-hidden="true" className="size-6"/>
                            </SheetClose>
                        </div>
                    </SheetHeader>

                    <div className="mt-8">
                        <CartProductList storeContext={storeContext} cart={cart}/>
                    </div>
                </div>

                <div className="border-t border-border px-4 py-6 sm:px-6">
                    {
                        cart && cart.products && cart.products.length > 0 && (
                            <>
                                <div className="space-y-2 border-t pt-6">
                                    <div className="flex justify-between text-base font-medium text-foreground">
                                        <p>{t('SUB_TOTAL')}</p>
                                        <p>{cart.displaySubTotal}</p>
                                    </div>
                                </div>
                                <p className="mt-0.5 text-sm text-neutral">
                                    {t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}
                                </p>
                                <div className="mt-6">
                                    <Link
                                        prefetch={false}
                                        href="/checkout"
                                        className="flex items-center justify-center rounded-md border border-transparent bg-primary px-6 py-3 text-base font-medium text-foreground shadow-xs"
                                    >
                                        {t('CHECKOUT')}
                                    </Link>
                                </div>
                            </>
                        )
                    }

                    <div className="mt-6 flex justify-center text-center text-sm text-neutral">
                        <p>
                            {t('OR')}{' '}
                            <SheetClose
                                type="button"
                                className="font-medium text-primary"
                            >
                                {t('CONTINUE_SHOPPING')}
                                <span
                                    aria-hidden="true"
                                    className="inline-block rtl:-scale-x-100"
                                >
                  {' '}
                                    &rarr;
                </span>
                            </SheetClose>
                        </p>
                    </div>
                </div>
            </SheetContent>
        </Sheet>
    );
}