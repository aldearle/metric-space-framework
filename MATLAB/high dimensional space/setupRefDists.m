

dataSeriesName = 'JS Reference Points';

threshold = 0.02;
pDist = ref_dists_2(3);

dists = zeros(9000,2);
dists(:,1) = ref_dists_2(1001:10000);
dists(:,2) = ref_dists_3(1001:10000);
