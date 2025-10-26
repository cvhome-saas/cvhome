'use client'
import {Store} from "@/types/store";
import {Button, Menu, MenuButton, MenuItem, MenuItems, Transition} from "@headlessui/react";
import {GlobeAltIcon} from "@heroicons/react/16/solid";
import {Fragment} from 'react';
import {usePathname, useRouter} from "@/i18n/navigation";
import {useTranslations} from "next-intl";

export const LanguageSelector = ({store, locale}: { store: Store, locale: string }) => {
    const t = useTranslations('COMPONENTS.HEADER.LANGUAGE');
    const router = useRouter();
    const pathname = usePathname();
    if (!store.supportedLanguages || store.supportedLanguages.length <= 1) {
        return <></>
    } else {
        const currentLocale = store.supportedLanguages.includes(locale) ? locale : 'en';
        return (
            <div className="relative inline-block text-start">
                <Menu as="div" className="relative">
                    <div>
                        <MenuButton
                            className="group inline-flex w-full justify-center items-center rounded-md border-0 bg-background px-4 py-2 text-sm font-medium text-neutral focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-background hover:bg-hover-neutral transition-colors duration-200">
                            <GlobeAltIcon
                                aria-hidden="true"
                                className="size-6 shrink-0 text-neutral group-hover:text-primary transition-transform duration-200 ease-in-out group-hover:scale-110"
                            />
                            <span
                                className="ms-2 text-sm font-medium text-foreground group-hover:text-primary transition-colors duration-200">
                                {t(currentLocale.toUpperCase())}
                            </span>
                            <span className="sr-only">{t('CHANGE_LANGUAGE')}</span>
                        </MenuButton>
                    </div>

                    <Transition
                        as={Fragment}
                        enter="transition ease-out duration-100"
                        enterFrom="transform opacity-0 scale-95"
                        enterTo="transform opacity-100 scale-100"
                        leave="transition ease-in duration-75"
                        leaveFrom="transform opacity-100 scale-100"
                        leaveTo="transform opacity-0 scale-95"
                    >
                        <MenuItems
                            className="absolute end-0 z-10 mt-2 w-auto origin-top-end rounded-md bg-background shadow-lg ring-1 ring-border ring-opacity-5 focus:outline-none">
                            <div className="py-1">
                                {store.supportedLanguages.map((it) => (
                                    <MenuItem key={it}>
                                        {({focus}) => (
                                            <Button onClick={() => router.push(pathname, {locale: it})}
                                                    className={`${
                                                        focus ? "bg-hover-neutral text-foreground" : "text-neutral"
                                                    } block px-4 py-2 text-sm w-full text-start transition-colors duration-150`}
                                            >
                                                {t(it.toUpperCase())}
                                            </Button>
                                        )}
                                    </MenuItem>
                                ))}
                            </div>
                        </MenuItems>
                    </Transition>
                </Menu>
            </div>
        );
    }
}