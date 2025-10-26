import {NextConfig} from 'next';
import createNextIntlPlugin from 'next-intl/plugin';

const nextConfig: NextConfig = {
    reactStrictMode: true,
    images: {
        remotePatterns: [
            {
                protocol: 'http',
                hostname: 'localhost',
                pathname: '**',
            },
            {
                protocol: 'https',
                hostname: '**',
                pathname: '**',
            },
        ]
    },

};
const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");
export default withNextIntl(nextConfig);
