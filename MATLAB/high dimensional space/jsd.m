function d = jsd(A,B)

Adash = normalise(A);
Bdash = normalise(B);

E = zeros(1,length(Adash));
for i = 1 : length(Adash)
    if Adash(i) > 0 && Bdash(i) > 0
        E(i) = h(Adash(i)) + h(Bdash(i)) - h(Adash(i) + Bdash(i));
    end
end
d = sqrt(1 - sum(E)/2);

end