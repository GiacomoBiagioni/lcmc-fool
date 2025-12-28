push 0
lhp

push function0
lhp
sw
lhp
push 1
add
shp
push 0
push -2
lfp
add
lw
lhp
stm
lhp
sw
lhp
push 1
add
shp
lhp
sw
lhp
push 1
add
shp
ltm
lfp
lfp
push -3
add
lw
stm
ltm
lw
push 0
add
lw
js
push 1
beq label2
push 0
b label3
label2:
push 1
label3:
print
halt

function0:
cfp
lra
lfp
lw
push -1
add
lw
push 2
bleq label0
push 0
b label1
label0:
push 1
label1:
stm
sra
pop
sfp
ltm
lra
js