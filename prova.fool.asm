push 6
push 6
div
push 2
beq label4
push 0
b label5
label4:
push 1
label5:
push 0
beq label2
push 5
push 5
beq label6
push 0
b label7
label6:
push 1
label7:
push 0
beq label2
push 1
b label3
label2:
push 0
label3:
push 1
beq label0
push 0
b label1
label0:
push 1
label1:
print
halt