function d = euc(A,B)

C = A - B;
C = C .* C;
d = sqrt(sum(C));

end