import socket 
import threading
import base64
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
from Crypto.Util.Padding import pad


class Cliente:
    def __init__(self):
        self.node = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        address = input('IP do servidor: ')
        port = int(input('PORTA do servidor: '))
        name = input('Qual o seu nickname: ')
        # address = "127.0.0.1"
        # port = 12345
        # name = "CIDA"
        port_and_ip = (address, port)
        self.node.connect(port_and_ip)
        print(f'\nConectado ao servidor {address} na porta {port}.\n')
        self.node.send(self.encrypt_aes(f'{name}').encode('utf-8'))
        self.node.send('\r\n'.encode('utf-8'))  #########
        print('################### SALA DE CHAT ###################\n')
    
    global key
    global _iv

    key = 'areyouokareyouok'
    _iv = "\x00"*AES.block_size # creates a 16 byte zero initialized string

    def decrypt_aes(self, cryptedStr):
        cipher = AES.new(key.encode(), AES.MODE_CBC, _iv.encode())
        cryptedStr_bytes = base64.b64decode(cryptedStr)
        recovery = cipher.decrypt(cryptedStr_bytes)
        return unpad(recovery, AES.block_size).decode('utf-8')

    def encrypt_aes(self, decryptedStr):
        b_decryptedStr = bytes(decryptedStr, encoding='utf-8')
        cipher = AES.new(key.encode(), AES.MODE_CBC, _iv.encode())
        ct_bytes = cipher.encrypt(pad(b_decryptedStr, AES.block_size))
        # iv = base64.b64encode(cipher.iv).decode('utf-8')
        ct = base64.b64encode(ct_bytes).decode('utf-8')
        return ct

    def send_sms(self, SMS):
        self.node.send(self.encrypt_aes(f'{SMS}').encode('utf-8')) #########
        self.node.send('\r\n'.encode('utf-8')) #########

    def receive_sms(self):
        while True:       
            data = self.node.recv(1024).decode('utf-8')
            print(self.decrypt_aes(data)) #########

    def main(self):
        while True:
            message = input()
            print('')
            self.send_sms(message)

if __name__ == '__main__':
    Client = Cliente()
    always_receive = threading.Thread(target=Client.receive_sms)
    always_receive.daemon = True
    always_receive.start()
    Client.main()