'use client'
import {forwardRef, useEffect, useState} from 'react'
import {LayoutParams} from "@/types/params";
import {Link, usePathname} from "@/i18n/navigation";
import {Cart} from "@/types/cart";
import {NavCartDialog} from "@/componantes/Nav/NavCartDialog";
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
import {Sheet, SheetContent, SheetTrigger} from "@/components/ui/sheet";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "@/components/ui/accordion";
import {cn} from "@/lib/utils";
import {Menu, ShoppingBag} from "lucide-react";
import {getCartManager} from "@/componantes/CartManager";


export const NavHeader = ({params}: { params: LayoutParams }) => {
    const t = useTranslations('COMPONENTS.HEADER');
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
    const [cartOpen, setCartOpen] = useState(false)
    const [cart, setCart] = useState<Cart | undefined>()
    const storeContext = params.storeContext;
    const cartManager = getCartManager(storeContext);
    const pathname = usePathname();

    useEffect(() => {
        setCartOpen(false);
        setMobileMenuOpen(false);
    }, [pathname]);

    useEffect(() => {
        const handleCartChange = () => {
            cartManager.consumeCart(setCart)
        };
        cartManager.subscribe(handleCartChange)
        return () => {
            cartManager.unsubscribe(handleCartChange)
        };
    }, [cartManager]);


    return (
        <>

            <nav aria-label="Global" className="mx-auto flex max-w-7xl items-center justify-between p-6 lg:px-8">
                <div className="flex lg:flex-1">
                    <Link prefetch={false} href={"/"} className="-m-1.5 p-1.5 group">
                        <span className="sr-only">{params.store.name}</span>
                        {
                            params.store.logo &&
                            <Image
                                alt={params.store.logo.name}
                                src={params.store.logo.path}
                                width={32}
                                height={32}
                                className="h-8 w-auto transition-transform duration-300 ease-in-out group-hover:scale-110"
                            />
                        }
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
                <NavigationMenu className="hidden lg:flex">
                    <NavigationMenuList>
                        <NavigationMenuItem>
                            <Link href="/" legacyBehavior passHref>
                                <NavigationMenuLink className={navigationMenuTriggerStyle()}>
                                    {t('HOME_TITLE')}
                                </NavigationMenuLink>
                            </Link>
                        </NavigationMenuItem>

                        {params.categories?.content?.filter(it => it.description).map((category) => (
                            <NavigationMenuItem key={category.code}>
                                {category.children && category.children.length > 0 ? (
                                    <>
                                        <Link href={`/category/${category.description.friendlyUrl}`} legacyBehavior passHref>
                                            <NavigationMenuTrigger className="cursor-pointer">
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
                                    <Link href={`/category/${category.description.friendlyUrl}`} legacyBehavior passHref>
                                        <NavigationMenuLink className={navigationMenuTriggerStyle()}>
                                            {category.description.name}
                                        </NavigationMenuLink>
                                    </Link>
                                )}
                            </NavigationMenuItem>
                        ))}

                        {params.contents?.content?.filter(it => it.linkToMenu && it.visible && it.description).map(it => (
                            <NavigationMenuItem key={it.code}>
                                <Link href={`/content/${it.description.friendlyUrl}`} legacyBehavior passHref>
                                    <NavigationMenuLink className={navigationMenuTriggerStyle()}>
                                        {it.description.name}
                                    </NavigationMenuLink>
                                </Link>
                            </NavigationMenuItem>
                        ))}
                    </NavigationMenuList>
                </NavigationMenu>

                <div className="hidden lg:flex lg:flex-1 lg:justify-end">
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
            <NavCartDialog storeContext={params.storeContext} cart={cart} setCart={setCart} cartOpen={cartOpen}
                           setCartOpen={setCartOpen}/>
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
                                        <Link href={`/category/${category.description.friendlyUrl}`} className="flex-grow px-3 py-2 text-base font-semibold leading-7 text-foreground">
                                            {category.description.name}
                                        </Link>
                                        <AccordionTrigger className="p-2 me-1.5" />
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

const ListItem = forwardRef<React.ElementRef<"a">, React.ComponentPropsWithoutRef<"a">>(
    ({className, title, children, ...props}, ref) => {
        return (
            <li>
                <NavigationMenuLink asChild>
                    <a
                        ref={ref}
                        className={cn(
                            "block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
                            className
                        )}
                        {...props}
                    >
                        <div className="text-sm font-medium leading-none">{title}</div>
                        <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
                            {children}
                        </p>
                    </a>
                </NavigationMenuLink>
            </li>
        );
    }
);
ListItem.displayName = "ListItem";