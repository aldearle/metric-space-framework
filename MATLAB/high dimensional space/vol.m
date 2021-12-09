function V = vol(a,b,c,aP,bP,cP)

m = [
    [0,a*a,b*b,c*c,1],
    [a*a,0,cP*cP,bP*bP,1,],
    [b*b,cP*cP,0,aP*aP,1],
    [c*c,bP*bP,aP*aP,0,1],
    [1,1,1,1,0]];

V = det(m);
end