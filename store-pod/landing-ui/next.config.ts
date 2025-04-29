import {NextConfig} from 'next';
import createNextIntlPlugin from 'next-intl/plugin';

const nextConfig: NextConfig = {
    output: "standalone",
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

const withNextIntl = createNextIntlPlugin();
export default withNextIntl(nextConfig);