import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Typed-Value',
  description: 'Type-safe identifiers for Kotlin Multiplatform',
  base: '/typed-value/',

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/typed-value/favicon.svg' }],
  ],

  themeConfig: {
    siteTitle: 'Typed-Value',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'Platforms', link: '/platforms/jvm' },
      { text: 'Integrations', link: '/integrations/jackson' },
    ],

    sidebar: {
      '/guide/': [
        {
          text: 'Introduction',
          items: [
            { text: 'Getting Started', link: '/guide/getting-started' },
            { text: 'Core Concepts', link: '/guide/core-concepts' },
          ]
        },
        {
          text: 'Usage',
          items: [
            { text: 'Convenience Types', link: '/guide/convenience-types' },
            { text: 'Utilities', link: '/guide/utilities' },
          ]
        }
      ],
      '/platforms/': [
        {
          text: 'Platforms',
          items: [
            { text: 'JVM', link: '/platforms/jvm' },
            { text: 'JavaScript', link: '/platforms/javascript' },
            { text: 'Native', link: '/platforms/native' },
          ]
        }
      ],
      '/integrations/': [
        {
          text: 'Framework Integrations',
          items: [
            { text: 'Jackson (JSON)', link: '/integrations/jackson' },
            { text: 'Spring MVC', link: '/integrations/spring' },
            { text: 'Hibernate (JPA)', link: '/integrations/hibernate' },
            { text: 'QueryDSL', link: '/integrations/querydsl' },
            { text: 'Elasticsearch', link: '/integrations/elasticsearch' },
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/ekino/typed-value' }
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright ekino'
    },

    search: {
      provider: 'local'
    },

    editLink: {
      pattern: 'https://github.com/ekino/typed-value/edit/main/docs/:path',
      text: 'Edit this page on GitHub'
    }
  }
})
