function d = tri(A,B)

Adash = normalise(A);
Bdash = normalise(B);
C = Adash - Bdash;
C = C .* C;
D = Adash + Bdash;
E = zeros(1,length(C));
for i = 1 : length(C)
    if D(i) > 0
        E(i) = C(i) / D(i);
    end
end
d = sqrt(sum(E)/2);

end