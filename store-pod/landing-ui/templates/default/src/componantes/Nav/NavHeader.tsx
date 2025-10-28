'use client'
import {
    Dialog,
    DialogPanel,
    Disclosure,
    DisclosureButton,
    DisclosurePanel,
    Popover,
    PopoverButton,
    PopoverGroup,
    PopoverPanel
} from "@headlessui/react";
import {Bars3Icon, XMarkIcon} from "@heroicons/react/24/outline";
import {ChevronDownIcon} from '@heroicons/react/20/solid'
import {useEffect, useState} from 'react'
import {LayoutParams} from "@/types/params";
import {Link, usePathname} from "@/i18n/navigation";
import {ShoppingBagIcon} from "@heroicons/react/16/solid";
import {Cart} from "@/types/cart";
import {getCartData} from "@/services/cart-utils";
import {emitter} from "next/client";
import {NavCartDialog} from "@/componantes/Nav/NavCartDialog";
import Image from 'next/image';
import {useTranslations} from "next-intl";


export const NavHeader = ({params}: { params: LayoutParams }) => {
    const t = useTranslations('COMPONENTS.HEADER');
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
    const [cartOpen, setCartOpen] = useState(false)
    const [cart, setCart] = useState<Cart | undefined>()


    const pathname = usePathname();

    useEffect(() => {
        setCartOpen(false);
        setMobileMenuOpen(false);
    }, [pathname]);

    useEffect(() => {
        setCart(getCartData());
        emitter.on("CART_STORAGE_CHANGED", data => {
            setCart(getCartData())
        });
    }, []);


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
                    <button
                        type="button"
                        onClick={() => setMobileMenuOpen(true)}
                        className="-m-2.5 inline-flex items-center justify-center rounded-md p-2.5 text-foreground"
                    >
                        <span className="sr-only">{t('OPEN_MENU')}</span>
                        <Bars3Icon aria-hidden="true" className="size-6"/>
                    </button>
                </div>
                <PopoverGroup className="hidden lg:flex lg:gap-x-12">
                    <Link prefetch={false} href={`/`}
                          className="text-sm/6 font-semibold text-foreground hover:text-primary">
                        {t('HOME_TITLE')}
                    </Link>

                    {params.categories && params.categories.content &&
                        params.categories.content.filter(it => it.description)
                            .map((it) => {
                                if (it.children && it.children.length > 0) {
                                    return (<Popover className="relative" key={it.code}>
                                        <div className="flex items-center gap-x-1">
                                            <Link prefetch={false} href={`/category/${it.description.friendlyUrl}`}
                                                  className="text-sm/6 font-semibold text-foreground hover:text-primary">
                                                {it.description.name}
                                            </Link>
                                            <PopoverButton
                                                className="flex items-center p-1 text-sm/6 font-semibold text-foreground focus:outline-none">
                                                <span className="sr-only">Open submenu</span>
                                                <ChevronDownIcon aria-hidden="true"
                                                                 className="size-5 flex-none text-neutral"/>
                                            </PopoverButton>
                                        </div>

                                        <PopoverPanel
                                            transition
                                            className="absolute top-full -start-8 z-10 mt-3 w-screen max-w-md overflow-hidden rounded-3xl bg-background ring-1 shadow-lg ring-border transition data-closed:translate-y-1 data-closed:opacity-0 data-enter:duration-200 data-enter:ease-out data-leave:duration-150 data-leave:ease-in"
                                        >
                                            <div className="p-4">
                                                {it.children
                                                    .filter(child => child.description)
                                                    .map((child) => (
                                                        <div
                                                            key={child.description.id}
                                                            className="group relative flex items-center gap-x-6 rounded-lg p-4 text-sm/6 hover:bg-hover-neutral"
                                                        >
                                                            <div className="flex-auto">
                                                                <Link prefetch={false}
                                                                      href={`/category/${child.description.friendlyUrl}`}
                                                                      className="block font-semibold text-foreground">
                                                                    {child.description.name}
                                                                    <span className="absolute inset-0"/>
                                                                </Link>
                                                            </div>
                                                        </div>
                                                    ))}
                                            </div>
                                        </PopoverPanel>
                                    </Popover>)
                                } else {
                                    return (<Link prefetch={false} href={`/category/${it.description.friendlyUrl}`}
                                                  key={it.code}
                                                  className="text-sm/6 font-semibold text-foreground hover:text-primary">
                                        {it.description.name}
                                    </Link>)
                                }

                            })}

                    {params.contents && params.contents.content &&
                        params.contents.content
                            .filter(it => it.linkToMenu)
                            .filter(it => it.visible)
                            .filter(it => it.description)
                            .map(it => {
                                return (
                                    <Link prefetch={false} href={`/content/${it.description.friendlyUrl}`}
                                          key={it.description.friendlyUrl}
                                          className="text-sm/6 font-semibold text-foreground hover:text-primary">
                                        {it.description.name}
                                    </Link>
                                )
                            })
                    }

                </PopoverGroup>
                <div className="hidden lg:flex lg:flex-1 lg:justify-end">

                    <a onClick={() => setCartOpen(true)}
                       className="group -m-2 flex items-center p-2 cursor-pointer rounded-full hover:bg-hover-neutral transition-colors duration-200">
                        <ShoppingBagIcon
                            aria-hidden="true"
                            className="size-6 shrink-0 text-neutral group-hover:text-primary transition-transform duration-200 ease-in-out group-hover:scale-110"
                        />
                        <span
                            className="ms-2 text-sm font-medium text-primary">{cart ? cart.quantity : '0'}</span>
                        <span className="sr-only">{t('OPEN_CART')}</span>
                    </a>


                </div>
            </nav>
            <Dialog open={mobileMenuOpen} onClose={setMobileMenuOpen} className="lg:hidden">
                <div className="fixed inset-0 z-10"/>
                <DialogPanel
                    className="fixed inset-y-0 inset-e-0 z-10 w-full overflow-y-auto bg-background px-6 py-6 sm:max-w-sm sm:ring-1 sm:ring-border">
                    <div className="flex items-center justify-between">
                        <Link prefetch={false} href={"/"} className="-m-1.5 p-1.5 group">
                            <span className="sr-only">
                                {params.store.name}
                            </span>
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
                        <button
                            type="button"
                            onClick={() => setMobileMenuOpen(false)}
                            className="-m-2.5 rounded-md p-2.5 text-foreground"
                        >
                            <span className="sr-only">{t('CLOSE_MENU')}</span>
                            <XMarkIcon aria-hidden="true" className="size-6"/>
                        </button>
                    </div>
                    <div className="mt-6 flow-root">
                        <div className="-my-6 divide-y divide-border">
                            <div className="space-y-2 py-6">
                                <Link prefetch={false} href={`/`}
                                      className="-mx-3 block rounded-lg px-3 py-2 text-base/7 font-semibold text-foreground hover:bg-hover-neutral">
                                    {t('HOME_TITLE')}
                                </Link>

                                {
                                    params.categories && params.categories.content &&
                                    params.categories.content
                                        .filter(it => it.description)
                                        .map(it => {
                                            if (it.children && it.children.length > 0) {
                                                return (<Disclosure as="div" className="-mx-3" key={it.code}>
                                                    <div
                                                        className="flex w-full items-center justify-between rounded-lg hover:bg-hover-neutral">
                                                        <Link prefetch={false}
                                                              href={`/category/${it.description.friendlyUrl}`}
                                                              className="flex-grow px-3 py-2 text-base/7 font-semibold text-foreground">
                                                            {it.description.name}
                                                        </Link>
                                                        <DisclosureButton
                                                            className="group p-2 me-1.5 text-base/7 font-semibold text-foreground">
                                                            <span className="sr-only">Open submenu</span>
                                                            <ChevronDownIcon aria-hidden="true"
                                                                             className="size-5 flex-none group-data-open:rotate-180 text-neutral"/>
                                                        </DisclosureButton>
                                                    </div>
                                                    <DisclosurePanel className="mt-2 space-y-2 ps-3">
                                                        {it.children
                                                            .filter(child => child.description)
                                                            .map((child) => (
                                                                <Link prefetch={false}
                                                                      href={`/category/${child.description.friendlyUrl}`}
                                                                      key={child.code}
                                                                      className="block rounded-lg px-3 py-2 text-sm/7 font-semibold text-foreground hover:bg-hover-neutral"
                                                                >
                                                                    {child.description.name}
                                                                </Link>
                                                            ))}
                                                    </DisclosurePanel>
                                                </Disclosure>)
                                            } else {
                                                return (
                                                    <Link prefetch={false}
                                                          href={`/category/${it.description.friendlyUrl}`} key={it.code}
                                                          className="-mx-3 block rounded-lg px-3 py-2 text-base/7 font-semibold text-foreground hover:bg-hover-neutral"
                                                    >
                                                        {it.description.name}
                                                    </Link>)
                                            }
                                        })
                                }


                                {
                                    params.contents && params.contents.content &&
                                    params.contents.content
                                        .filter(it => it.linkToMenu)
                                        .filter(it => it.visible)
                                        .filter(it => it.description)
                                        .map(it => {
                                            return (
                                                <Link prefetch={false} href={`/content/${it.description.friendlyUrl}`}
                                                      key={it.code}
                                                      className="-mx-3 block rounded-lg px-3 py-2 text-base/7 font-semibold text-foreground hover:bg-hover-neutral">
                                                    {it.description.name}
                                                </Link>
                                            )
                                        })
                                }

                            </div>
                            <div className="py-6">
                                <a onClick={() => setCartOpen(true)}
                                   className="group -m-2 flex items-center p-2 cursor-pointer rounded-full hover:bg-hover-neutral transition-colors duration-200">
                                    <ShoppingBagIcon
                                        aria-hidden="true"
                                        className="size-6 shrink-0 text-neutral group-hover:text-primary transition-transform duration-200 ease-in-out group-hover:scale-110"
                                    />
                                    <span
                                        className="ms-2 text-sm font-medium text-primary">{cart ? cart.quantity : '0'}</span>
                                    <span className="sr-only">{t('OPEN_CART')}</span>
                                </a>
                            </div>
                        </div>
                    </div>
                </DialogPanel>
            </Dialog>
            <NavCartDialog storeContext={params.storeContext} cart={cart} setCart={setCart} cartOpen={cartOpen}
                           setCartOpen={setCartOpen}/>
        </>
    )
}