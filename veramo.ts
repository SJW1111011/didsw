// 使用Veramo创建绑定生物特征的DID
import { createAgent, IResolver, IDIDManager } from '@veramo/core'
import { DIDManager, MemoryDIDStore } from '@veramo/did-manager'
import { KeyManager, MemoryKeyStore, MemoryPrivateKeyStore } from '@veramo/key-manager'

// 初始化Agent
const agent = createAgent<IDIDManager & IResolver>({
  plugins: [
    new KeyManager({
      store: new MemoryKeyStore(),
      importKey: async (args) => {
        // 从Android Keystore获取公钥（实际需通过JNI调用）
        const publicKey = await fetchFromNativeLayer(args.alias)
        return { ...publicKey, type: 'Secp256k1' }
      },
    }),
    new DIDManager({
      store: new MemoryDIDStore(),
      defaultProvider: 'did:ethr',
    }),
  ],
})

// 创建DID
const did = await agent.didManagerCreate({
  provider: 'did:ethr',
  options: {
    keyType: 'Secp256k1',
    // 绑定到设备生物特征密钥
    privateKeyHex: 'external:android_keystore:user_did_key', 
  },
})