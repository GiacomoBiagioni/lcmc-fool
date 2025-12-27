push 6
push 6
div
push 2
beq label2
push 0
b label3
label2:
push 1
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